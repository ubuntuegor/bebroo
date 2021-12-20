package to.bnt.draw.shared.drawing.drawing_structures

data class Line(private val linePoints: List<Point> = emptyList(), var strokeWidth: Double, var strokeColor: String) {
    private var _points = linePoints.toMutableList()

    private var smallestXCoordinate = _points.minOfOrNull { it.x } ?: Double.POSITIVE_INFINITY
    private var smallestYCoordinate = _points.minOfOrNull { it.y } ?: Double.POSITIVE_INFINITY
    private var biggestXCoordinate = _points.maxOfOrNull { it.x } ?: Double.NEGATIVE_INFINITY
    private var biggestYCoordinate = _points.maxOfOrNull { it.y } ?: Double.NEGATIVE_INFINITY

    init {
        initializeExtremeCoordinatesValues()
    }

    fun getContainingRectangle(): Rectangle? {
        if (_points.isEmpty()) return null
        return Rectangle(
            Point(smallestXCoordinate, biggestYCoordinate),
            Point(biggestXCoordinate, smallestYCoordinate)
        )
    }

    private fun initializeExtremeCoordinatesValues() {
        _points.forEach { updateExtremeCoordinatesValues(it) }
    }

    private fun updateExtremeCoordinatesValues(addedPoint: Point) {
        if (addedPoint.x > biggestXCoordinate) biggestXCoordinate = addedPoint.x
        if (addedPoint.x < smallestXCoordinate) smallestXCoordinate = addedPoint.x
        if (addedPoint.y > biggestYCoordinate) biggestYCoordinate = addedPoint.y
        if (addedPoint.y < smallestYCoordinate) smallestYCoordinate = addedPoint.y
    }

    fun findNearestPointTo(otherPoint: Point): Double =
        (_points.minOf { it.calculateQuadraticDistanceTo(otherPoint) })

    fun addPoint(point: Point) {
        _points.add(point)
        updateExtremeCoordinatesValues(point)
    }
    fun clear() {
        _points = mutableListOf()
    }

    val points: List<Point>
        get() = _points
}