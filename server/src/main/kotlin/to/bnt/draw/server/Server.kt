package to.bnt.draw.server

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import to.bnt.draw.server.helper.getBoardByUuid

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.main() {
    val developmentMode = environment.config.property("ktor.development").getString().toBoolean()
    val appName = environment.config.property("app.name").getString()

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Compression)
    if (!developmentMode) {
        install(HttpsRedirect)
    }

    routing {
        static("/assets") {
            resources("assets")
        }

        get("/board/{uuid}") {
            val title = call.parameters["uuid"]?.let { getBoardByUuid(it) }?.let { it.name + " - " + appName }
            call.respond(
                FreeMarkerContent(
                    "page.ftl",
                    mapOf(
                        "title" to title
                    )
                )
            )
        }

        get("/{...}") {
            call.respond(
                FreeMarkerContent(
                    "page.ftl",
                    mapOf(
                        "title" to appName
                    )
                )
            )
        }
    }
}
