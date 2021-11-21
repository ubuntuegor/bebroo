package to.bnt.draw.server.api.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.MissingParameterException
import to.bnt.draw.server.models.Users
import java.security.MessageDigest
import java.util.*

fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun createToken(environment: ApplicationEnvironment, userId: Int): String {
    val secret = environment.config.property("jwt.secret").getString()
    val expiresIn = 7 * 24 * 60 * 60 * 1000
    return JWT.create()
        .withClaim("id", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
        .sign(Algorithm.HMAC256(secret))
}

fun Route.login() {
    get("/login") {
        val username = call.request.queryParameters["username"] ?: throw MissingParameterException("username")
        val password = call.request.queryParameters["password"] ?: throw MissingParameterException("password")

        val userId = transaction {
            val user = Users.select { Users.username eq username }.firstOrNull()
                ?: throw ApiException("Такой пользователь не найден")

            val hash = user[Users.hash] ?: throw ApiException("Вход невозможен")

            if (hash == hashPassword(password)) {
                user[Users.id].value
            } else {
                throw ApiException("Неверный пароль")
            }
        }

        val token = createToken(application.environment, userId)
        call.respond(mapOf("token" to token))
    }
}

fun Route.signup() {
    post("/signup") {
        val parameters = call.receiveParameters()
        val username = parameters["username"] ?: throw MissingParameterException("username")
        val password = parameters["password"] ?: throw MissingParameterException("password")
        val displayName = parameters["displayName"] ?: throw MissingParameterException("displayName")

        if (username.isEmpty() || password.isEmpty() || displayName.isEmpty())
            throw ApiException("Ошибка запроса: поля не могут быть пустыми")

        val hash = hashPassword(password)

        val userId = transaction {
            if (!Users.slice(Users.id).select { Users.username eq username }.empty())
                throw ApiException("Пользователь с таким логином уже существует")

            Users.insertAndGetId {
                it[Users.username] = username
                it[Users.hash] = hash
                it[Users.displayName] = displayName
            }.value
        }

        val token = createToken(application.environment, userId)
        call.respond(mapOf("token" to token))
    }
}
