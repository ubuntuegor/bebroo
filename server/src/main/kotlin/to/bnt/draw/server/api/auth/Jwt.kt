package to.bnt.draw.server.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.models.Users
import java.util.*

object TokenVerifier {
    private var verifier: JWTVerifier? = null
    fun getSingleton(secret: String): JWTVerifier =
        verifier ?: JWT.require(Algorithm.HMAC256(secret)).build().also { verifier = it }
}

fun ApplicationEnvironment.getJWTSecret() = this.config.property("jwt.secret").getString()

fun Authentication.Configuration.jwtUser(environment: ApplicationEnvironment) {
    jwt("user") {
        val secret = environment.getJWTSecret()

        verifier(
            TokenVerifier.getSingleton(secret)
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

fun Application.createToken(userId: Int): String {
    val secret = environment.getJWTSecret()
    val expiresIn = 7 * 24 * 60 * 60 * 1000
    return JWT.create()
        .withClaim("id", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
        .sign(Algorithm.HMAC256(secret))
}

fun Application.getUserIdFromToken(token: String): Int? {
    val secret = environment.getJWTSecret()
    val verifier = TokenVerifier.getSingleton(secret)
    return try {
        verifier.verify(token).parsePayload().getClaim("id")?.asInt()
    } catch (e: JWTVerificationException) {
        null
    }
}

fun ApplicationCall.getUserId(): Int? = this.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asInt()

private fun DecodedJWT.parsePayload(): Payload {
    val payloadString = String(Base64.getUrlDecoder().decode(payload))
    return JWTParser().parsePayload(payloadString)
}
