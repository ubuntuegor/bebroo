package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.Point

data class Paint(val fillColor: String = "#000000", val strokeColor: String = "#000000", val strokeWidth: Double = .0)

interface SharedCanvas {
    val width: Int
    val height: Int
    fun bindEvents(board: DrawingBoard)
    fun drawLine(points: List<Point>, paint: Paint)
    fun clear()
    fun cleanup()
}
