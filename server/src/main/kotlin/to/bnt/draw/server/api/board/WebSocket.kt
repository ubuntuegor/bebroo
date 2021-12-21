package to.bnt.draw.server.api.board

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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

suspend fun DefaultWebSocketServerSession.checkBoardAccessAndClose(boardUuid: UUID, userId: Int?): Boolean {
    val hasAccess = transaction {
        val board =
            Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull() ?: return@transaction false

        board[Boards.isPublic] || board[Users.id].value == userId
    }
    if (!hasAccess) close()
    return hasAccess
}

private fun DecodedJWT.parsePayload(): Payload {
    val payloadString = String(Base64.getUrlDecoder().decode(payload))
    return JWTParser().parsePayload(payloadString)
}

fun Route.boardWebSocket() {
    val secret = application.environment.config.property("jwt.secret").getString()
    val jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build()

    fun getId(token: String): Payload? {
        return try {
            jwtVerifier.verify(token).parsePayload()
        } catch (e: JWTVerificationException) {
            null
        }
    }

    val connectionsForBoard = Collections.synchronizedMap(mutableMapOf<UUID, MutableSet<Connection>>())

    webSocket("/{uuid}/websocket") {
        val userId = call.request.queryParameters["token"]?.let { getId(it)?.getClaim("id")?.asInt() }
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
                    .orderBy(Figures.id to SortOrder.ASC).map {
                    Figure(
                        id = it[Figures.id].value,
                        drawingData = it[Figures.drawingData],
                        color = it[Figures.color],
                        strokeWidth = it[Figures.strokeWidth]
                    )
                }
            }
            figures.forEach {
                sendAction(AddFigure(figure = it))
            }
        }

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

            // notify others of user connected
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
                        val localId = action.localId
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
                                localId?.let { localId -> it.session.sendAction(FigureAck(localId, figureId)) }
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
