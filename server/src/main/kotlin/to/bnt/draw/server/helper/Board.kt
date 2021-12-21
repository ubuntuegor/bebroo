package to.bnt.draw.server.helper

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import to.bnt.draw.server.models.Boards
import to.bnt.draw.server.models.Users
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import java.util.*

fun getBoardByUuid(uuid: String): Board? {
    val boardUuid = try {
        UUID.fromString(uuid)
    } catch (e: IllegalArgumentException) {
        return null
    }

    return transaction {
        val board = Boards.innerJoin(Users).select { Boards.id eq boardUuid }.firstOrNull()

        board?.let {
            if (!it[Boards.isPublic]) {
                null
            } else {
                Board(
                    uuid = board[Boards.id].value.toString(),
                    name = board[Boards.name],
                    creator = User(
                        id = board[Users.id].value,
                        displayName = board[Users.displayName],
                        avatarUrl = board[Users.avatarUrl]
                    ),
                    isPublic = it[Boards.isPublic]
                )
            }
        }
    }

}
