package to.bnt.draw.server.api.users

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import to.bnt.draw.server.api.httpClient
import to.bnt.draw.server.models.Users

@Serializable
data class GoogleInfo(
    val id: String,
    val name: String,
    val picture: String
)

suspend fun getGoogleInfo(token: String): GoogleInfo {
    return httpClient.get("https://www.googleapis.com/userinfo/v2/me") {
        headers {
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

fun Route.googleOAuth() {
    authenticate("google-oauth") {
        get("/googleAuthorize") {
            // Redirects to Google automatically
        }

        get("/googleCallback") {
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            if (principal == null) {
                call.respond(
                    FreeMarkerContent(
                        "googleOAuthError.ftl",
                        mapOf("errorText" to "Не удалось выполнить вход")
                    )
                )
            } else {
                val oauthToken = principal.accessToken
                val googleInfo = getGoogleInfo(oauthToken)

                val userId = transaction {
                    val user = Users.select { Users.googleId eq googleInfo.id }.firstOrNull()
                    if (user != null) {
                        Users.update({ Users.googleId eq googleInfo.id }) {
                            it[displayName] = googleInfo.name
                            it[avatarUrl] = googleInfo.picture
                        }
                        user[Users.id].value
                    } else {
                        Users.insertAndGetId {
                            it[googleId] = googleInfo.id
                            it[displayName] = googleInfo.name
                            it[avatarUrl] = googleInfo.picture
                        }.value
                    }
                }

                val token = createToken(application.environment, userId)
                val targetOrigin = application.environment.config.property("googleOAuth.targetOrigin").getString()
                call.respond(
                    FreeMarkerContent(
                        "googleOAuthSuccess.ftl",
                        mapOf(
                            "token" to token,
                            "targetOrigin" to targetOrigin
                        )
                    )
                )
            }
        }
    }
}
