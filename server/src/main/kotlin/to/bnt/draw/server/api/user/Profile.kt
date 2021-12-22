package to.bnt.draw.server.api.user

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import to.bnt.draw.server.api.auth.getUserId
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.InvalidTokenException
import to.bnt.draw.server.models.Users
import to.bnt.draw.server.models.toUser

fun Route.getCurrentUser() {
    authenticate("user") {
        get("/me") {
            val userId = call.getUserId() ?: throw InvalidTokenException()
            val response = transaction {
                Users.select { Users.id eq userId }.first().toUser()
            }
            call.respond(response)
        }
    }
}

fun Route.modifyCurrentUser() {
    authenticate("user") {
        patch("/me") {
            val userId = call.getUserId() ?: throw InvalidTokenException()
            val parameters = call.receiveParameters()
            transaction {
                Users.update({ Users.id eq userId }) {
                    parameters["displayName"]?.let { field ->
                        if (field.isEmpty()) throw ApiException("Поля не могут быть пустыми")
                        it[displayName] = field
                    }
                }
            }
            call.respond(emptyMap<String, String>())
        }
    }
}
