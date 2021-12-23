package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import to.bnt.draw.shared.structures.User

object Users : IntIdTable() {
    val username = varchar("username", 100).uniqueIndex().nullable()
    val hash = text("hash").nullable()
    val googleId = text("google_id").uniqueIndex().nullable()
    val displayName = varchar("display_name", 100)
    val avatarUrl = text("avatar_url").nullable()
}

fun ResultRow.toUser() =
    User(
        id = this[Users.id].value,
        displayName = this[Users.displayName],
        avatarUrl = this[Users.avatarUrl]
    )
