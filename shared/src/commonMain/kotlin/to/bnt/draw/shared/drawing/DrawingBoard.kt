package to.bnt.draw.shared.drawing

class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var isMouseDown = false

    fun onMouseDown(point: Point) {
        isMouseDown = true
        canvas.drawCircle(point, 10.0, Paint("#ee1111"))
    }

    fun onMouseMove(point: Point) {
        if (isMouseDown)
            canvas.drawCircle(point, 10.0, Paint("#ee1111"))
    }

    fun onMouseUp(point: Point) {
        isMouseDown = false
    }
}
