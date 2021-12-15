package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.Point

class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var isMouseDown = false
    private val line = mutableListOf<Point>()
    val paint = Paint(strokeWidth = 15.0, strokeColor = "#93faa5")

    fun onMouseDown(point: Point) {
        isMouseDown = true
        line.clear()
    }

    fun onMouseMove(point: Point) {
        if (isMouseDown) {
            line.add(point)
            canvas.drawLine(line, paint)
        }
    }

    fun onMouseUp(point: Point) {
        line.add(point)
        canvas.drawLine(line, paint)
        isMouseDown = false
    }
}
