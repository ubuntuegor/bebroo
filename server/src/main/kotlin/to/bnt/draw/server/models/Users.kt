package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val username = varchar("username", 100).uniqueIndex().nullable()
    val hash = text("hash").nullable()
    val googleId = text("google_id").uniqueIndex().nullable()
    val displayName = varchar("display_name", 100)
    val avatarUrl = text("avatar_url")
}
