package to.bnt.draw.server

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.models.*

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

    routing {
        route("/api") {
            get("/test") {
                call.respondText("OK")
            }
        }
    }
}
