package to.bnt.draw.server.api.board

import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.ForbiddenException
import to.bnt.draw.server.models.Boards
import to.bnt.draw.server.models.Figures
import to.bnt.draw.server.models.Users
import to.bnt.draw.shared.structures.*
import java.util.*
import kotlin.run

class Connection(val userId: Int?, val session: DefaultWebSocketServerSession)

suspend fun DefaultWebSocketServerSession.sendAction(action: Action) {
    send(Frame.Text(action.toJson()))
}

suspend fun DefaultWebSocketServerSession.checkBoardAccess(boardUuid: UUID, userId: Int?): Boolean {
    val hasAccess = userId?.let {
        transaction {
            val board =
                Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull() ?: return@transaction false

            !(!board[Boards.isPublic] && board[Users.id].value != userId)
        }
    } ?: false
    if (!hasAccess) close()
    return hasAccess
}

fun Route.boardWebSocket() {
    val connectionsForBoard = Collections.synchronizedMap(mutableMapOf<UUID, MutableSet<Connection>>())

    webSocket("/{uuid}/websocket") {
        val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
        val uuidParameter = call.parameters["uuid"] ?: throw RuntimeException("Trying to open board without uuid")
        val boardUuid = try {
            UUID.fromString(uuidParameter)
        } catch (e: IllegalArgumentException) {
            throw ApiException("Такой доски не существует")
        }

        // check privileges
        transaction {
            val board = Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()
                ?: throw ApiException("Такой доски не существует")

            if (!board[Boards.isPublic] && board[Users.id].value != userId) {
                throw ForbiddenException()
            }
        }

        val lastFigureId = call.request.queryParameters["figureId"]?.toIntOrNull()
        lastFigureId?.let {
            val figures = transaction {
                Figures.select { (Figures.board eq boardUuid) and (Figures.id greater lastFigureId) }.map {
                    Figure(
                        id = it[Figures.id].value,
                        drawingData = it[Figures.drawingData],
                        color = it[Figures.color],
                        strokeWidth = it[Figures.strokeWidth]
                    )
                }
            }
            sendAction(AddFigures(figures))
        }

        val connection = Connection(userId, this)
        connectionsForBoard[boardUuid] ?: run {
            connectionsForBoard[boardUuid] = Collections.synchronizedSet(mutableSetOf())
        }
        val connections = connectionsForBoard[boardUuid]!!
        connections.add(connection)

        try {
            // send connected users
            val connectedUsers = connections.filter { it.userId != null }.map {
                transaction {
                    val userEntry = Users.select { Users.id eq it.userId }.first()
                    User(
                        id = userEntry[Users.id].value,
                        displayName = userEntry[Users.displayName],
                        avatarUrl = userEntry[Users.avatarUrl]
                    )
                }
            }
            sendAction(ConnectedUsers(connectedUsers))

            // notify of user connected
            userId?.let {
                val user = transaction {
                    val userEntry = Users.select { Users.id eq userId }.first()
                    User(
                        id = userEntry[Users.id].value,
                        displayName = userEntry[Users.displayName],
                        avatarUrl = userEntry[Users.avatarUrl]
                    )
                }
                connections.forEach {
                    if (it.userId == userId) return@forEach
                    it.session.sendAction(UserConnected(user))
                }
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                // unauthorized users can't contribute
                userId ?: continue

                when (val action = Action.fromJson(frame.readText())) {
                    is AddFigures -> {
                        if (!checkBoardAccess(boardUuid, userId)) continue
                        val addedFigures = mutableListOf<Figure>()
                        action.figures.forEach { figure ->
                            val figureId = transaction {
                                Figures.insertAndGetId {
                                    it[board] = boardUuid
                                    it[drawingData] = figure.drawingData
                                    it[color] = figure.color
                                    it[strokeWidth] = figure.strokeWidth
                                }
                            }
                            addedFigures.add(
                                Figure(
                                    id = figureId.value,
                                    drawingData = figure.drawingData,
                                    color = figure.color,
                                    strokeWidth = figure.strokeWidth
                                )
                            )
                        }
                        connections.forEach {
                            if (it.userId == userId) return@forEach
                            if (!checkBoardAccess(boardUuid, it.userId)) return@forEach
                            it.session.sendAction(AddFigures(addedFigures))
                        }
                    }
                    is RemoveFigure -> {
                        if (!checkBoardAccess(boardUuid, userId)) continue
                        transaction {
                            Figures.deleteWhere { Figures.id eq action.figureId }
                        }
                        connections.forEach {
                            if (it.userId == userId) return@forEach
                            if (!checkBoardAccess(boardUuid, it.userId)) return@forEach
                            it.session.sendAction(RemoveFigure(action.figureId))
                        }
                    }
                }
            }
        } finally {
            connections.remove(connection)
            userId?.let {
                connections.forEach {
                    it.session.sendAction(UserDisconnected(userId))
                }
            }
        }
    }
}
