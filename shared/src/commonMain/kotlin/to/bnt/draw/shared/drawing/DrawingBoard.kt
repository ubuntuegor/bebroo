package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.LineConversionStorage
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.line_algorithms.simplifyLine

class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var isDrawing = false
    private val drawingLine = Line()
    private val conversionStorage = LineConversionStorage(canvas.width, canvas.height)
    private var scaleCoefficient = 1.0
    private var isDragging = false
    private var lastMousePoint = Point(0.0, 0.0)
    val paint = Paint(strokeWidth = 1.0, strokeColor = "#fab0be")

    fun onMouseDown(point: Point) {
        isDrawing = true
    }

    fun onMouseMove(point: Point) {
        when {
            isDragging -> {
                conversionStorage.translateCamera((lastMousePoint - point).apply { y = -y })
                redrawLines()
            }
            isDrawing -> drawingLine.addPoint(point)
        }
        lastMousePoint = point
    }

    fun onWheelUp() {
        isDragging = false
    }

    fun onWheelDown() {
        isDragging = true
        stopDrawing()
    }

    fun onMouseUp(point: Point) {
        drawingLine.addPoint(point)
        stopDrawing()
    }

    fun onMouseWheel(wheelDelta: Double) {
        if (isDrawing || isDragging) return
        changeScaleCoefficient(calculateScaleCoefficient(wheelDelta))
    }

    fun stopDrawing() {
        if (!isDrawing) return
        drawLine(simplifyLine(drawingLine, SIMPLIFICATION_EPSILON))
        drawingLine.clear()
        isDrawing = false
    }

    private fun calculateScaleCoefficient(wheelDelta: Double): Double {
        val scaleFactor = if (wheelDelta >= 0) 1.0 / WHEEL_DELTA_FACTOR else WHEEL_DELTA_FACTOR
        return scaleFactor * scaleCoefficient
    }

    fun drawLine(simplifiedLine: Line) {
        canvas.drawLine(conversionStorage.addLine(simplifiedLine)?.points ?: return, paint)
    }

    private fun redrawLines() {
        canvas.clear()
        conversionStorage.getDisplayingLines().forEach { canvas.drawLine(it.points, paint) }
    }

    fun clearLineAtPoint(point: Point) {
        canvas.clear()
        conversionStorage.removeLineAtPoint(point)
        redrawLines()
    }

    private fun changeScaleCoefficient(scaleCoefficient: Double) {
        if (scaleCoefficient <= 0) return
        conversionStorage.changeScaleCoefficient(scaleCoefficient)
        this.scaleCoefficient = scaleCoefficient
        redrawLines()
    }

    companion object {
        private const val SIMPLIFICATION_EPSILON = 2.0
        private const val WHEEL_DELTA_FACTOR = 1.05
    }
}
