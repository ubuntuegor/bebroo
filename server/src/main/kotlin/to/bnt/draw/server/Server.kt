package to.bnt.draw.server

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.main(testing: Boolean = false) {
    routing {
        get("/{...}") {
            call.respondText("Hello, world!")
        }
    }
}
