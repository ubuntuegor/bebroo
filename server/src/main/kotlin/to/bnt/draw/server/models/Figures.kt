package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.LongIdTable

object Figures : LongIdTable() {
    val board = reference("board", Boards)
    val drawingData = text("drawing_data")
    val color = varchar("color", 8)
    val strokeWidth = integer("stroke_width")
}
