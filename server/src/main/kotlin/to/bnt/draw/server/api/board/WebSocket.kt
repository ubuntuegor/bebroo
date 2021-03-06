package to.bnt.draw.server.api.board

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.auth.getUserIdFromToken
import to.bnt.draw.server.models.*
import to.bnt.draw.shared.structures.*
import java.util.*
import kotlin.run

class Connection(val userId: Int?, val session: DefaultWebSocketServerSession)

suspend fun DefaultWebSocketServerSession.sendAction(action: Action) {
    send(Frame.Text(action.toJson()))
}

suspend fun DefaultWebSocketServerSession.checkBoardAccessAndClose(boardUuid: UUID, userId: Int?): Boolean {
    val board = transaction {
        Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()
    }

    return (board?.checkAccessToBoard(userId) ?: false)
        .also { if (!it) close() }
}

fun Route.boardWebSocket() {
    val connectionsForBoard = Collections.synchronizedMap(mutableMapOf<UUID, MutableSet<Connection>>())

    webSocket("/{uuid}/websocket") {
        val userId =
            call.request.queryParameters["token"]?.let { application.getUserIdFromToken(it) }
        val uuidParameter = call.parameters["uuid"] ?: throw RuntimeException("Trying to open board without uuid")
        val boardUuid = try {
            UUID.fromString(uuidParameter)
        } catch (e: IllegalArgumentException) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Такой доски не существует"))
            return@webSocket
        }

        if (!checkBoardAccessAndClose(boardUuid, userId)) return@webSocket

        val connection = Connection(userId, this)
        connectionsForBoard[boardUuid] ?: run {
            connectionsForBoard[boardUuid] = Collections.synchronizedSet(mutableSetOf())
        }
        val connections = connectionsForBoard[boardUuid]!!
        connections.add(connection)

        val lastFigureId = call.request.queryParameters["figureId"]?.toIntOrNull()
        lastFigureId?.let {
            val figures = transaction {
                Figures.select { (Figures.board eq boardUuid) and (Figures.id greater lastFigureId) }
                    .orderBy(Figures.id to SortOrder.ASC).map { it.toFigure() }
            }
            figures.forEach {
                sendAction(AddFigure(figure = it))
            }
        }

        try {
            // send connected users
            val connectedUsers = connections.filter { it.userId != null }.map {
                transaction {
                    Users.select { Users.id eq it.userId }.first().toUser()
                }
            }
            sendAction(ConnectedUsers(connectedUsers))

            // notify others of user connected
            userId?.let {
                val user = transaction {
                    Users.select { Users.id eq userId }.first().toUser()
                }
                connections.forEach {
                    if (it == connection) return@forEach
                    it.session.sendAction(UserConnected(user))
                }
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                // unauthorized users can't contribute
                userId ?: continue

                when (val action = Action.fromJson(frame.readText())) {
                    is AddFigure -> {
                        if (!checkBoardAccessAndClose(boardUuid, userId)) continue
                        val figure = action.figure
                        val figureId = transaction {
                            Figures.insertAndGetId {
                                it[board] = boardUuid
                                it[drawingData] = figure.drawingData
                                it[color] = figure.color
                                it[strokeWidth] = figure.strokeWidth
                            }
                        }.value
                        figure.id = figureId
                        connections.forEach {
                            if (!checkBoardAccessAndClose(boardUuid, it.userId)) return@forEach
                            if (it == connection) {
                                action.localId?.let { localId -> it.session.sendAction(FigureAck(localId, figureId)) }
                            } else {
                                it.session.sendAction(AddFigure(figure = figure))
                            }
                        }
                    }
                    is RemoveFigure -> {
                        if (!checkBoardAccessAndClose(boardUuid, userId)) continue
                        transaction {
                            Figures.deleteWhere { Figures.id eq action.figureId }
                        }
                        connections.forEach {
                            if (!checkBoardAccessAndClose(boardUuid, it.userId)) return@forEach
                            if (it == connection) return@forEach
                            it.session.sendAction(RemoveFigure(action.figureId))
                        }
                    }
                    else -> {}
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
