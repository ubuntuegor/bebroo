package to.bnt.draw.server.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.users.*
import to.bnt.draw.server.models.*

val httpClient = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        })
    }
}

fun Application.initializeDatabase() {
    Database.connect(
        environment.config.property("database.url").getString(), driver = "org.postgresql.Driver",
        user = environment.config.property("database.username").getString()
    )
    transaction {
        SchemaUtils.create(Users, Boards, UsersToBoards, Figures)
    }
}

fun Application.api(testing: Boolean = false) {
    initializeDatabase()

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
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

        oauth("google-oauth") {
            urlProvider = { environment.config.property("googleOAuth.redirectUrl").getString() }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                    accessTokenUrl = "https://oauth2.googleapis.com/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                )
            }
            client = httpClient
        }
    }

    install(StatusPages) {
        exception<ApiException> { cause ->
            call.respondText(cause.message, status = cause.errorCode)
        }
    }

    routing {
        route("/api") {
            route("/users") {
                login()
                signup()
                googleOAuth()
                getCurrentUser()
                modifyCurrentUser()
            }
        }
    }
}
