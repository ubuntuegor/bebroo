package to.bnt.draw.server

import com.auth0.jwt.JWT
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.models.Boards
import to.bnt.draw.server.models.Users
import to.bnt.draw.server.models.UsersToBoards
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val testEnv = createTestEnvironment {
    config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
}

class ApiTest {
    private val username = "testUser"
    private val password = "testPassword"
    private var token: String? = null
    private var privateBoardUUID: String? = null
    private var publicBoardUUID: String? = null

    private fun deleteUser() {
        transaction {
            val user = Users.select { Users.username eq username }.firstOrNull() ?: return@transaction
            val boards = UsersToBoards.select { UsersToBoards.user eq user[Users.id].value }.map { it[UsersToBoards.board].value }
            UsersToBoards.deleteWhere { UsersToBoards.user eq user[Users.id].value }
            boards.forEach {
                Boards.deleteWhere { Boards.id eq it }
            }
            Users.deleteWhere { Users.username eq username }
        }
    }

    @Test
    fun mainTest(): Unit = withApplication(testEnv) {
        deleteUser()
        // TEST USER
        // Signup
        with(handleRequest(HttpMethod.Post, "/api/auth/signup") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "username" to username,
                    "password" to password,
                    "displayName" to "This User"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val data: Map<String, String> = Json.decodeFromString(response.content!!)
            val tokenData = JWT.decode(data["token"])
            assertNotNull(tokenData.getClaim("id").asInt())
        }

        // Login
        handleRequest(
            HttpMethod.Get,
            "/api/auth/login?" + listOf("username" to username, "password" to password).formUrlEncode()
        ).apply {
            assertEquals(HttpStatusCode.OK, response.status())
            val data: Map<String, String> = Json.decodeFromString(response.content!!)
            val tokenData = JWT.decode(data["token"])
            assertNotNull(tokenData.getClaim("id").asInt())
            token = data["token"]
        }

        // Modify user
        with(handleRequest(HttpMethod.Patch, "/api/user/me") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "displayName" to "That User"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        // Get me
        with(handleRequest(HttpMethod.Get, "/api/user/me") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val user: User = Json.decodeFromString(response.content!!)
            assertEquals("That User", user.displayName)
        }

        // BOARDS
        // Create
        with(handleRequest(HttpMethod.Post, "/api/board/create") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "name" to "My Board"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val data: Map<String, String> = Json.decodeFromString(response.content!!)
            privateBoardUUID = data["uuid"]
        }

        with(handleRequest(HttpMethod.Post, "/api/board/create") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "name" to "Public Board"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val data: Map<String, String> = Json.decodeFromString(response.content!!)
            publicBoardUUID = data["uuid"]
        }

        // Modify
        with(handleRequest(HttpMethod.Patch, "/api/board/$publicBoardUUID") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "isPublic" to "true"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        // TEST BAD VALUES
        // Signup
        with(handleRequest(HttpMethod.Post, "/api/auth/signup") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "username" to "",
                    "password" to "a",
                    "displayName" to "a"
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        // Login
        handleRequest(
            HttpMethod.Get,
            "/api/auth/login?" + listOf("username" to "", "password" to "a").formUrlEncode()
        ).apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        // Modify user
        with(handleRequest(HttpMethod.Patch, "/api/user/me") {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "displayName" to ""
                ).formUrlEncode()
            )
        }) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }

        // TEST WITHOUT AUTH
        // Open public
        handleRequest(HttpMethod.Get, "/api/board/$publicBoardUUID").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            val board: Board = Json.decodeFromString(response.content!!)
            assertEquals("Public Board", board.name)
        }

        // Open private
        handleRequest(HttpMethod.Get, "/api/board/$privateBoardUUID").apply {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }
}
