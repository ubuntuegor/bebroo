package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.UUIDTable

object Boards : UUIDTable() {
    val name = varchar("name", 200)
    val creator = reference("creator", Users)
    val isPublic = bool("is_public").default(false)
}
