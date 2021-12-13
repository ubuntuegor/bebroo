package to.bnt.draw.server.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersToBoards : Table() {
    val user = reference("user", Users)
    val board = reference("board", Boards)
    val lastOpened = timestamp("lastOpened")
}
