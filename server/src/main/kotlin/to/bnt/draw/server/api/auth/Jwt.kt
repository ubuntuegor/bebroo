package to.bnt.draw.server.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.models.Users

fun Authentication.Configuration.jwtUser(environment: ApplicationEnvironment) {
    jwt("user") {
        val secret = environment.config.property("jwt.secret").getString()

        verifier(
            JWT.require(Algorithm.HMAC256(secret)).build()
        )

        validate { credential ->
            val userId = credential.payload.getClaim("id").asInt() ?: return@validate null
            val userExists = transaction {
                Users.select { Users.id eq userId }.empty().not()
            }
            if (userExists) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }
    }
}