package to.bnt.draw.server.api.board

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.ForbiddenException
import to.bnt.draw.server.api.exceptions.InvalidTokenException
import to.bnt.draw.server.api.exceptions.MissingParameterException
import to.bnt.draw.server.models.Boards
import to.bnt.draw.server.models.Figures
import to.bnt.draw.server.models.Users
import to.bnt.draw.server.models.UsersToBoards
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.Figure
import to.bnt.draw.shared.structures.User
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*

fun Route.listBoards() {
    authenticate("user") {
        get("/list") {
            val userId =
                call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt() ?: throw InvalidTokenException()
            val response = transaction {
                val boards = UsersToBoards
                    .innerJoin(Boards)
                    .innerJoin(Users, { Boards.creator }, { Users.id })
                    .select { UsersToBoards.user eq userId }
                boards.map {
                    Board(
                        uuid = it[Boards.id].value.toString(),
                        name = it[Boards.name],
                        creator = User(
                            id = it[Users.id].value,
                            displayName = it[Users.displayName],
                            avatarUrl = it[Users.avatarUrl]
                        ),
                        isPublic = it[Boards.isPublic],
                        timestamp = it[UsersToBoards.lastOpened].epochSecond
                    )
                }
            }
            call.respond(response)
        }
    }
}

fun Route.openBoard() {
    authenticate("user", optional = true) {
        get("/{uuid}") {
            val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()
            val uuidParameter = call.parameters["uuid"] ?: throw RuntimeException("Trying to open board without uuid")
            val boardUuid = try {
                UUID.fromString(uuidParameter)
            } catch (e: IllegalArgumentException) {
                throw ApiException("Такой доски не существует")
            }

            val showContributors = call.request.queryParameters["showContributors"].toBoolean()
            val showFigures = call.request.queryParameters["showFigures"].toBoolean()

            val response = transaction {
                val board = Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()
                    ?: throw ApiException("Такой доски не существует")

                if (!board[Boards.isPublic] && board[Users.id].value != userId) {
                    throw ForbiddenException()
                }

                // Update user who opened the board
                userId.let {
                    val userToBoardCondition =
                        Op.build { (UsersToBoards.board eq boardUuid) and (UsersToBoards.user eq userId) }
                    val userToBoard = UsersToBoards.select(userToBoardCondition).firstOrNull()
                    userToBoard?.let {
                        UsersToBoards.update({ userToBoardCondition }) {
                            it[lastOpened] = Instant.now()
                        }
                    } ?: UsersToBoards.insert {
                        it[UsersToBoards.board] = boardUuid
                        it[user] = userId
                        it[lastOpened] = Instant.now()
                    }
                }

                val contributors = if (showContributors) {
                    UsersToBoards.innerJoin(Users).select { UsersToBoards.board eq boardUuid }.map {
                        User(
                            id = it[Users.id].value,
                            displayName = it[Users.displayName],
                            avatarUrl = it[Users.avatarUrl]
                        )
                    }
                } else {
                    null
                }

                val figures = if (showFigures) {
                    Figures.select { Figures.board eq boardUuid }.map {
                        Figure(
                            id = it[Figures.id].value,
                            drawingData = it[Figures.drawingData],
                            color = it[Figures.color],
                            strokeWidth = it[Figures.strokeWidth]
                        )
                    }
                } else {
                    null
                }

                Board(
                    uuid = board[Boards.id].value.toString(),
                    name = board[Boards.name],
                    creator = User(
                        id = board[Users.id].value,
                        displayName = board[Users.displayName],
                        avatarUrl = board[Users.avatarUrl]
                    ),
                    isPublic = board[Boards.isPublic],
                    contributors = contributors,
                    figures = figures
                )
            }

            call.respond(response)
        }
    }
}

fun Route.modifyBoard() {
    authenticate("user") {
        patch("/{uuid}") {
            val userId =
                call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt() ?: throw InvalidTokenException()
            val uuidParameter = call.parameters["uuid"] ?: throw RuntimeException("Trying to open board without uuid")
            val boardUuid = try {
                UUID.fromString(uuidParameter)
            } catch (e: IllegalArgumentException) {
                throw ApiException("Такой доски не существует")
            }

            val parameters = call.receiveParameters()

            transaction {
                val board = Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()
                    ?: throw ApiException("Такой доски не существует")

                if (board[Users.id].value != userId) {
                    throw ForbiddenException()
                }

                Boards.update({ Boards.id eq boardUuid }) {
                    parameters["name"]?.let { field ->
                        if (field.isEmpty()) throw ApiException("Поля не могут быть пустыми")
                        it[name] = field
                    }
                    parameters["isPublic"]?.let { field ->
                        it[isPublic] = field.toBoolean()
                    }
                }
            }

            call.respond(emptyMap<String, String>())
        }
    }
}

fun Route.createBoard() {
    authenticate("user") {
        post("/create") {
            val userId =
                call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt() ?: throw InvalidTokenException()

            val parameters = call.receiveParameters()
            val name = parameters["name"] ?: throw MissingParameterException("name")

            if (name.isEmpty())
                throw ApiException("Поля не могут быть пустыми")

            val boardUuid = transaction {
                Boards.insertAndGetId {
                    it[Boards.name] = name
                    it[creator] = userId
                    it[isPublic] = false
                }
            }

            call.respond(mapOf("uuid" to boardUuid.toString()))
        }
    }
}
