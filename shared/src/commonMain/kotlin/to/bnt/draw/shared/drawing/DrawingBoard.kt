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
    private val convertionStorage = LineConversionStorage()
    private var scaleCoefficient = 1.0
    val paint = Paint(strokeWidth = 1.0, strokeColor = "#fab0be")

    fun onMouseDown(point: Point) {
        isMouseDown = true
        drawingLine.clear()
    }

    fun onMouseMove(point: Point) {
        if (isMouseDown) {
            drawingLine.addPoint(point)
        }
    }

    fun onMouseUp(point: Point) {
        drawingLine.addPoint(point)
        isMouseDown = false
        drawLine(simplifyLine(drawingLine, SIMPLIFICATION_EPSILON))
    }

    private fun calculateScaleCoefficient(wheelDelta: Int): Double {
        return scaleCoefficient * wheelDelta * WHEEL_DELTA_FACTOR
    }

    fun onMouseWheel(wheelDelta: Int) {
        changeScaleCoefficient(calculateScaleCoefficient(wheelDelta))
        redrawLines()
    }

    fun drawLine(simplifiedLine: Line) {
        canvas.drawLine(convertionStorage.addLine(simplifiedLine).points, paint)
    }

    private fun redrawLines() {
        canvas.clear()
        convertionStorage.getDisplayingLines().forEach { canvas.drawLine(it.points, paint) }
    }

    fun clearLineAtPoint(point: Point) {
        canvas.clear()
        convertionStorage.removeLineAtPoint(point)
        redrawLines()
    }

    private fun changeScaleCoefficient(scaleCoefficient: Double) {
        if (scaleCoefficient <= 0) return
        convertionStorage.changeScaleCoefficient(scaleCoefficient)
        this.scaleCoefficient = scaleCoefficient
        redrawLines()
    }

    companion object {
        private const val SIMPLIFICATION_EPSILON = 2.0
        private const val WHEEL_DELTA_FACTOR = 0.1
    }
}
