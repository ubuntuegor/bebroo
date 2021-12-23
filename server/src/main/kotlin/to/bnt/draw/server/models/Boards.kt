package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.Figure
import to.bnt.draw.shared.structures.User

object Boards : UUIDTable() {
    val name = varchar("name", 200)
    val creator = reference("creator", Users)
    val isPublic = bool("is_public").default(false)
}

fun ResultRow.toBoard(contributors: List<User>? = null, figures: List<Figure>? = null) =
    Board(
        uuid = this[Boards.id].value.toString(),
        name = this[Boards.name],
        creator = this.toUser(),
        isPublic = this[Boards.isPublic],
        timestamp = this.getOrNull(UsersToBoards.lastOpened)?.epochSecond,
        contributors = contributors,
        figures = figures
    )
