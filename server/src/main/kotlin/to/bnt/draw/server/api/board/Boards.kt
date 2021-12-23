package to.bnt.draw.server.api.board

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.auth.getUserId
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.ForbiddenException
import to.bnt.draw.server.api.exceptions.InvalidTokenException
import to.bnt.draw.server.api.exceptions.MissingParameterException
import to.bnt.draw.server.models.*
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*

fun ResultRow.checkAccessToBoard(userId: Int?) =
    this[Boards.isPublic] || this[Users.id].value == userId

fun Route.listBoards() {
    authenticate("user") {
        get("/list") {
            val userId = call.getUserId() ?: throw InvalidTokenException()
            val response = transaction {
                val boards = UsersToBoards
                    .innerJoin(Boards)
                    .innerJoin(Users, { Boards.creator }, { Users.id })
                    .select { UsersToBoards.user eq userId }
                    .orderBy(UsersToBoards.lastOpened to SortOrder.DESC_NULLS_FIRST)
                boards.map { it.toBoard() }
            }
            call.respond(response)
        }
    }
}

fun Route.openBoard() {
    authenticate("user", optional = true) {
        get("/{uuid}") {
            val userId = call.getUserId()
            val uuidParameter = call.parameters["uuid"] ?: throw RuntimeException("Trying to open board without uuid")
            val boardUuid = try {
                UUID.fromString(uuidParameter)
            } catch (e: IllegalArgumentException) {
                throw ApiException("Такой доски не существует")
            }

            val showContributors = call.request.queryParameters["showContributors"].toBoolean()
            val showFigures = call.request.queryParameters["showFigures"].toBoolean()

            val response = transaction {
                val boardRow = Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()
                    ?: throw ApiException("Такой доски не существует")

                if (!boardRow.checkAccessToBoard(userId)) {
                    userId?.let { throw ForbiddenException() }
                        ?: throw InvalidTokenException()
                }

                // Update user who opened the board
                userId?.let {
                    val userToBoardCondition =
                        Op.build { (UsersToBoards.board eq boardUuid) and (UsersToBoards.user eq userId) }
                    val userToBoard = UsersToBoards.select(userToBoardCondition).firstOrNull()
                    userToBoard?.let {
                        UsersToBoards.update({ userToBoardCondition }) {
                            it[lastOpened] = Instant.now()
                        }
                    } ?: UsersToBoards.insert {
                        it[board] = boardUuid
                        it[user] = userId
                        it[lastOpened] = Instant.now()
                    }
                }

                val contributors = if (showContributors) {
                    UsersToBoards.innerJoin(Users)
                        .select { UsersToBoards.board eq boardUuid }.map { it.toUser() }
                } else {
                    null
                }

                val figures = if (showFigures) {
                    Figures.select { Figures.board eq boardUuid }
                        .orderBy(Figures.id to SortOrder.ASC).map { it.toFigure() }
                } else {
                    null
                }

                boardRow.toBoard(contributors, figures)
            }

            call.respond(response)
        }
    }
}

fun Route.modifyBoard() {
    authenticate("user") {
        patch("/{uuid}") {
            val userId = call.getUserId() ?: throw InvalidTokenException()
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
            val userId = call.getUserId() ?: throw InvalidTokenException()

            val parameters = call.receiveParameters()
            val name = parameters["name"] ?: throw MissingParameterException("name")

            if (name.isEmpty())
                throw ApiException("Поля не могут быть пустыми")

            val boardUuid = transaction {
                val uuid = Boards.insertAndGetId {
                    it[Boards.name] = name
                    it[creator] = userId
                    it[isPublic] = false
                }

                UsersToBoards.insert {
                    it[board] = uuid
                    it[user] = userId
                    it[lastOpened] = Instant.now()
                }

                uuid.value
            }

            call.respond(mapOf("uuid" to boardUuid.toString()))
        }
    }
}
