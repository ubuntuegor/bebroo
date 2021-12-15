package to.bnt.draw.server.api

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.auth.*
import to.bnt.draw.server.api.board.*
import to.bnt.draw.server.api.user.*
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

fun Application.api() {
    initializeDatabase()

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwtUser(environment)
        googleOauth(environment)
    }

    install(StatusPages) {
        exception<ApiException> { cause ->
            call.respondText(cause.message, status = cause.errorCode)
        }
    }

    install(WebSockets)

    routing {
        route("/api") {
            route("/auth") {
                login()
                signup()
                googleOAuth()
                googleIdToken()
            }
            route("/user") {
                getCurrentUser()
                modifyCurrentUser()
            }
            route("/board") {
                listBoards()
                openBoard()
                modifyBoard()
                createBoard()
                boardWebSocket()
            }
            route("/{...}") {
                // 404 override
                get {
                    call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }
            }
        }
    }
}
