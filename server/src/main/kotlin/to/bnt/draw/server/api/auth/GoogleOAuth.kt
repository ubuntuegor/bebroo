package to.bnt.draw.server.api.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.request.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import to.bnt.draw.server.api.exceptions.ApiException
import to.bnt.draw.server.api.exceptions.MissingParameterException
import to.bnt.draw.server.api.httpClient
import to.bnt.draw.server.models.Users


fun Authentication.Configuration.googleOauth(environment: ApplicationEnvironment) {
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

fun updateUserAndGetId(googleInfo: GoogleInfo): Int {
    return transaction {
        val user = Users.select { Users.googleId eq googleInfo.id }.firstOrNull()
        user?.let {
            Users.update({ Users.googleId eq googleInfo.id }) {
                it[avatarUrl] = googleInfo.picture
            }
            user[Users.id].value
        } ?: Users.insertAndGetId {
            it[googleId] = googleInfo.id
            it[displayName] = googleInfo.name
            it[avatarUrl] = googleInfo.picture
        }.value
    }
}

fun Route.googleOAuth() {
    authenticate("google-oauth") {
        get("/googleAuthorize") {
            // Redirects to Google automatically
        }

        get("/googleCallback") {
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            val targetOrigin = application.environment.config.property("googleOAuth.targetOrigin").getString()
            principal?.let {
                val oauthToken = principal.accessToken

                val googleInfo = getGoogleInfo(oauthToken)
                val userId = updateUserAndGetId(googleInfo)
                val token = application.createToken(userId)

                call.respond(
                    FreeMarkerContent(
                        "googleOAuthSuccess.ftl",
                        mapOf(
                            "token" to token,
                            "targetOrigin" to targetOrigin
                        )
                    )
                )
            } ?: call.respond(
                FreeMarkerContent(
                    "googleOAuthError.ftl",
                    mapOf(
                        "errorText" to "Не удалось выполнить вход",
                        "targetOrigin" to targetOrigin
                    )
                )
            )
        }
    }
}

fun Route.googleIdToken() {
    val verifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance()
    ).setAudience(listOf(System.getenv("GOOGLE_CLIENT_ID"))).build()


    post("/googleIdToken") {
        val parameters = call.receiveParameters()
        val idTokenString = parameters["idToken"] ?: throw MissingParameterException("idToken")

        val idToken = verifier.verify(idTokenString)

        idToken?.let {
            val payload = idToken.payload

            val googleInfo = GoogleInfo(
                id = payload.subject,
                name = payload["name"] as String,
                picture = payload["picture"] as String
            )

            val userId = updateUserAndGetId(googleInfo)
            val token = application.createToken(userId)

            call.respond(mapOf("token" to token))
        } ?: throw ApiException("Не удалось верифицировать токен")
    }
}
