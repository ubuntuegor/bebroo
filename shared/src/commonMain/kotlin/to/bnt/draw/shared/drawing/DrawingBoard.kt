package to.bnt.draw.shared.drawing

import to.bnt.draw.shared.drawing.drawing_structures.AddLineResult
import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.LineConversionStorage
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.line_algorithms.simplifyLine

open class DrawingBoard(private val canvas: SharedCanvas) {
    init {
        canvas.bindEvents(this)
    }

    private var _strokeColor = defaultColors[0]
    private var _strokeWidth = defaultStrokeWidth
    private var isDrawing = false
    private var isDragging = false
    protected val conversionStorage = LineConversionStorage(canvas.width, canvas.height)
    private var scaleCoefficient = 1.0
    private var lastMousePoint = Point(0.0, 0.0)
    private val drawingLine = Line(emptyList(), _strokeWidth * scaleCoefficient, _strokeColor)

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
            drawingLine.strokeWidth = value * scaleCoefficient
        }
    var onScaleChanged: (Double) -> Unit = {}

    open fun onMouseDown(point: Point) {
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
                        Paint(strokeColor = drawingLine.strokeColor, strokeWidth = drawingLine.strokeWidth)
                    )
                }
            }
        }
        lastMousePoint = point
    }

    fun onMouseUp(point: Point) {
        if (isDrawing && !isEraser) drawingLine.addPoint(point)
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
        val scaleFactor = if (wheelDelta >= 0) 1.0 / WHEEL_DELTA_FACTOR else WHEEL_DELTA_FACTOR
        changeScaleCoefficient(scaleFactor * scaleCoefficient)
    }

    fun setScale(newScale: Double) {
        changeScaleCoefficient(newScale)
    }

    fun onResize() {
        conversionStorage.changeScreenSize(canvas.width, canvas.height)
        redrawLines()
    }

    protected open fun stopDrawing(): AddLineResult? {
        if (!isDrawing) return null
        val result = if (drawingLine.points.isNotEmpty()) {
            val simplifiedLine = simplifyLine(drawingLine, SIMPLIFICATION_EPSILON)
            val addResult = conversionStorage.addLine(simplifiedLine)
            addResult?.screenSmoothedLine?.let {
                canvas.drawLine(it.points, Paint(strokeColor = it.strokeColor, strokeWidth = it.strokeWidth))
            }
            addResult
        } else null
        drawingLine.clear()
        isDrawing = false
        redrawLines()
        return result
    }

    protected fun redrawLines() {
        canvas.clear()
        conversionStorage.getDisplayingLines()
            .forEach { canvas.drawLine(it.points, Paint(strokeColor = it.strokeColor, strokeWidth = it.strokeWidth)) }
        canvas.drawLine(
            drawingLine.points,
            Paint(strokeColor = drawingLine.strokeColor, strokeWidth = drawingLine.strokeWidth)
        )
    }

    protected open fun clearLineAtPoint(point: Point): Long? {
        return conversionStorage.removeLineAtPoint(point)?.also {
            redrawLines()
        }
    }

    private fun changeScaleCoefficient(scaleCoefficient: Double) {
        if (scaleCoefficient <= 0) return
        val coercedCoefficient = scaleCoefficient.coerceIn(scaleRange)
        conversionStorage.changeScaleCoefficient(coercedCoefficient)
        this.scaleCoefficient = coercedCoefficient
        drawingLine.strokeWidth = _strokeWidth * coercedCoefficient
        onScaleChanged(coercedCoefficient)
        redrawLines()
    }

    open fun cleanup() {
        canvas.cleanup()
    }

    companion object {
        val defaultColors = listOf("#F85353", "#F8B653", "#38CA46", "#388CCA", "#9238CA", "#FA78C6", "#3C3C3C")
        val strokeWidthRange = 4..40
        const val defaultStrokeWidth = 10
        private val scaleRange = 0.2..5.0
        private const val SIMPLIFICATION_EPSILON = 2.0
        private const val WHEEL_DELTA_FACTOR = 1.05
    }
}
