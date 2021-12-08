package to.bnt.draw.server.api.users

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.InvalidTokenException
import to.bnt.draw.server.models.Users
import to.bnt.draw.shared.structures.User

fun Route.getCurrentUser() {
    authenticate("user") {
        get("/me") {
            val userId =
                call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt() ?: throw InvalidTokenException()
            val user = transaction {
                val user = Users.select { Users.id eq userId }.first()
                User(user[Users.id].value, user[Users.displayName], user[Users.avatarUrl])
            }
            call.respond(user)
        }
    }
}

fun Route.modifyCurrentUser() {
    authenticate("user") {
        patch("/me") {
            val userId =
                call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt() ?: throw InvalidTokenException()
            val parameters = call.receiveParameters()
            transaction {
                if (parameters.contains("displayName")) {
                    if (parameters["displayName"]!!.isEmpty()) throw ApiException("Поля не могут быть пустыми")
                    Users.update({ Users.id eq userId }) {
                        it[displayName] = parameters["displayName"]!!
                    }
                }
            }
            call.respond(emptyMap<String, String>())
        }
    }
}
