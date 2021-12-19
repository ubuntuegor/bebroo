package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.LineConversionStorage
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.line_algorithms.simplifyLine

class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var _strokeColor = defaultColors[0]
    private var _strokeWidth = defaultStrokeWidth
    private var isDrawing = false
    private var isDragging = false
    private val drawingLine = Line(emptyList(), _strokeWidth.toDouble(), _strokeColor)
    private val conversionStorage = LineConversionStorage(canvas.width, canvas.height)
    private var scaleCoefficient = 1.0
    private var lastMousePoint = Point(0.0, 0.0)

    var isEraser = false
    var strokeColor: String
        get() = _strokeColor
        set(value) {
            _strokeColor = value
            drawingLine.strokeColor = value
        }
    var strokeWidth: Int
        get() = _strokeWidth
        set(value) {
            _strokeWidth = value
            drawingLine.strokeWidth = value.toDouble()
        }
    var onScaleChanged: (Double) -> Unit = {}

    fun onMouseDown(point: Point) {
        isDrawing = true
        if (!isEraser) drawingLine.addPoint(point)
    }

    fun onMouseMove(point: Point) {
        when {
            isDragging -> {
                conversionStorage.translateCamera((lastMousePoint - point).apply { y = -y })
                redrawLines()
            }
            isDrawing -> {
                if (isEraser) {
                    clearLineAtPoint(point)
                } else {
                    drawingLine.addPoint(point)
                    canvas.drawLine(
                        drawingLine.points,
                        Paint(strokeColor = _strokeColor, strokeWidth = _strokeWidth * scaleCoefficient)
                    )
                }
            }
        }
        lastMousePoint = point
    }

    fun onMouseUp(point: Point) {
        if (isEraser) return
        if (isDrawing) drawingLine.addPoint(point)
        stopDrawing()
    }

    fun onWheelDown() {
        isDragging = true
        stopDrawing()
    }

    fun onWheelUp() {
        isDragging = false
    }

    fun onMouseWheel(wheelDelta: Double) {
        if (isDrawing || isDragging) return
        changeScaleCoefficient(calculateScaleCoefficient(wheelDelta))
    }

    fun setScale(newScale: Double) {
        changeScaleCoefficient(newScale)
    }

    fun onResize() {
        redrawLines()
    }

    private fun stopDrawing() {
        if (!isDrawing) return
        val simplifiedLine = simplifyLine(drawingLine, SIMPLIFICATION_EPSILON)
        val smoothLine = conversionStorage.addLine(simplifiedLine)
        smoothLine?.let {
            canvas.drawLine(smoothLine.points, Paint(strokeColor = it.strokeColor, strokeWidth = it.strokeWidth))
        }
        drawingLine.clear()
        isDrawing = false
        redrawLines()
    }

    private fun calculateScaleCoefficient(wheelDelta: Double): Double {
        val scaleFactor = if (wheelDelta >= 0) 1.0 / WHEEL_DELTA_FACTOR else WHEEL_DELTA_FACTOR
        return scaleFactor * scaleCoefficient
    }

    private fun redrawLines() {
        canvas.clear()
        conversionStorage.getDisplayingLines()
            .forEach { canvas.drawLine(it.points, Paint(strokeColor = it.strokeColor, strokeWidth = it.strokeWidth)) }
        canvas.drawLine(
            drawingLine.points,
            Paint(strokeColor = drawingLine.strokeColor, strokeWidth = drawingLine.strokeWidth)
        )
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
        onScaleChanged(scaleCoefficient)
        redrawLines()
    }

    fun cleanup() {
        canvas.cleanup()
    }

    companion object {
        val defaultColors = listOf("#F85353", "#F8B653", "#38CA46", "#388CCA", "#9238CA", "#FA78C6", "#3C3C3C")
        val strokeWidthRange = 4..40
        const val defaultStrokeWidth = 10
        private const val SIMPLIFICATION_EPSILON = 2.0
        private const val WHEEL_DELTA_FACTOR = 1.05
    }
}
