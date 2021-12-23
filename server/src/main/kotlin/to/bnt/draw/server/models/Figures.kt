package to.bnt.draw.server.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import to.bnt.draw.shared.structures.Figure

object Figures : LongIdTable() {
    val board = reference("board", Boards)
    val drawingData = text("drawing_data")
    val color = varchar("color", 8)
    val strokeWidth = integer("stroke_width")
}

fun ResultRow.toFigure() =
    Figure(
        id = this[Figures.id].value,
        drawingData = this[Figures.drawingData],
        color = this[Figures.color],
        strokeWidth = this[Figures.strokeWidth]
    )
