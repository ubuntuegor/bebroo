package to.bnt.draw.shared.apiClient

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import to.bnt.draw.shared.apiClient.exceptions.*
import to.bnt.draw.shared.structures.Action
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User

class ApiClient(internal val apiUrl: String, var token: String? = null) {
    private val client = HttpClient {
        install(WebSockets)
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        HttpResponseValidator {
            handleResponseException { exception ->
                when (exception) {
                    is ClientRequestException -> {
                        if (exception.response.status == HttpStatusCode.Unauthorized) throw InvalidTokenException()
                        else if (exception.response.status == HttpStatusCode.Forbidden) throw ForbiddenException()
                        throw ApiException(exception.response.readText())
                    }
                    is ServerResponseException -> {
                        throw UnexpectedServerErrorException()
                    }
                    else -> throw RequestErrorException()
                }
            }
        }
    }

    private fun HttpRequestBuilder.appendToken() {
        token?.let {
            header("Authorization", "Bearer $it")
        }
    }

    internal val authEndpoint = "/auth"

    suspend fun signup(username: String, password: String, displayName: String): String {
        val endpoint = "$authEndpoint/signup"
        val response: Map<String, String> = client.submitForm(apiUrl + endpoint, Parameters.build {
            append("username", username)
            append("password", password)
            append("displayName", displayName)
        }, false) {
            method = HttpMethod.Post
        }

        return response["token"] ?: throw ApiException("Ошибка получения токена")
    }

    suspend fun login(username: String, password: String): String {
        val endpoint = "$authEndpoint/login"
        val response: Map<String, String> = client.get(apiUrl + endpoint) {
            parameter("username", username)
            parameter("password", password)
        }

        return response["token"] ?: throw ApiException("Ошибка получения токена")
    }

    suspend fun googleOAuthIdToken(idToken: String): String {
        val endpoint = "$authEndpoint/googleIdToken"
        val response: Map<String, String> = client.submitForm(apiUrl + endpoint, Parameters.build {
            append("idToken", idToken)
        }, false) {
            method = HttpMethod.Post
        }

        return response["token"] ?: throw ApiException("Ошибка получения токена")
    }

    private val userEndpoint = "/user"

    suspend fun getMe(): User {
        val endpoint = "$userEndpoint/me"
        return client.get(apiUrl + endpoint) {
            appendToken()
        }
    }

    suspend fun modifyMe(displayName: String) {
        val endpoint = "$userEndpoint/me"
        client.submitForm<HttpResponse>(apiUrl + endpoint, Parameters.build {
            append("displayName", displayName)
        }, false) {
            method = HttpMethod.Patch
            appendToken()
        }
    }

    private val boardEndpoint = "/board"

    suspend fun listBoards(): List<Board> {
        val endpoint = "$boardEndpoint/list"
        return client.get(apiUrl + endpoint) {
            appendToken()
        }
    }

    suspend fun getBoard(uuid: String, showContributors: Boolean = false, showFigures: Boolean = false): Board {
        val endpoint = "$boardEndpoint/$uuid"
        return client.get(apiUrl + endpoint) {
            parameter("showContributors", showContributors)
            parameter("showFigures", showFigures)
            appendToken()
        }
    }

    suspend fun modifyBoard(uuid: String, name: String? = null, isPublic: Boolean? = null) {
        val endpoint = "$boardEndpoint/$uuid"
        client.submitForm<HttpResponse>(apiUrl + endpoint, Parameters.build {
            name?.let {
                append("name", it)
            }
            isPublic?.let {
                append("isPublic", it.toString())
            }
        }, false) {
            method = HttpMethod.Patch
            appendToken()
        }
    }

    suspend fun createBoard(name: String): String {
        val endpoint = "$boardEndpoint/create"
        val response: Map<String, String> = client.submitForm(apiUrl + endpoint, Parameters.build {
            append("name", name)
        }, false) {
            method = HttpMethod.Post
            appendToken()
        }

        return response["uuid"] ?: throw ApiException("Ошибка создания доски")
    }

    suspend fun boardWebSocket(
        uuid: String,
        figureId: Long?,
        onConnected: (DefaultClientWebSocketSession) -> Unit,
        onAction: (Action) -> Unit
    ) {
        val wsUrl = apiUrl.replaceFirst("https", "wss").replaceFirst("http", "ws")
        val endpoint = "$boardEndpoint/$uuid/websocket"
        client.webSocket(wsUrl + endpoint, {
            token?.let {
                parameter("token", token)
            }
            figureId?.let {
                parameter("figureId", figureId)
            }
        }) {
            onConnected(this)
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                onAction(Action.fromJson(frame.readText()))
            }
        }
    }

    fun close() {
        client.close()
    }
}
