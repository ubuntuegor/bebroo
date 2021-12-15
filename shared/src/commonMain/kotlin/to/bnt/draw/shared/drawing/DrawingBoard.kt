package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.LineConversionStorage
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.points_algorithms.simplifyLine

class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var isMouseDown = false
    private val drawingLine = Line()
    private val conversionStorage = LineConversionStorage(canvas.width, canvas.height)
    private var scaleCoefficient = 1.0
    val paint = Paint(strokeWidth = 1.0, strokeColor = "#fab0be")

    fun onMouseDown(point: Point) {
        isMouseDown = true
        drawingLine.clear()
    }

    fun onMouseMove(point: Point) {
        println(point)
        if (isMouseDown) {
            drawingLine.addPoint(point)
        }
    }

    fun onMouseUp(point: Point) {
        drawingLine.addPoint(point)
        isMouseDown = false
        drawLine(simplifyLine(drawingLine, SIMPLIFICATION_EPSILON))
    }

    private fun calculateScaleCoefficient(wheelDelta: Double): Double {
        val scaleFactor = if (wheelDelta >= 0) 1.0 / WHEEL_DELTA_FACTOR else WHEEL_DELTA_FACTOR
        return scaleFactor * scaleCoefficient
    }

    fun onMouseWheel(wheelDelta: Double) {
        changeScaleCoefficient(calculateScaleCoefficient(wheelDelta))
    }

    fun drawLine(simplifiedLine: Line) {
        canvas.drawLine(conversionStorage.addLine(simplifiedLine).points, paint)
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
