package to.bnt.draw.shared.drawing.drawing_structures

data class Line(private val linePoints: List<Point> = emptyList()) {
    private var _points = linePoints.toMutableList()

    var smallestXCoordinate = _points.minOfOrNull { it.x } ?: Double.POSITIVE_INFINITY
    var smallestYCoordinate = _points.minOfOrNull { it.y } ?: Double.POSITIVE_INFINITY
    var biggestXCoordinate = _points.maxOfOrNull { it.x } ?: Double.NEGATIVE_INFINITY
    var biggestYCoordinate = _points.maxOfOrNull { it.y } ?: Double.NEGATIVE_INFINITY

    fun getContainingRectangle(): Rectangle? {
        if (_points.isEmpty()) return null
        return Rectangle(
            Point(smallestXCoordinate, biggestYCoordinate),
            Point(biggestXCoordinate, smallestYCoordinate)
        )
    }

    fun updateExtremeCoorinatesValues(addedPoint: Point) {
        if (addedPoint.x > biggestXCoordinate) biggestXCoordinate = addedPoint.x
        if (addedPoint.x < smallestXCoordinate) smallestXCoordinate = addedPoint.x
        if (addedPoint.y > biggestYCoordinate) biggestYCoordinate = addedPoint.y
        if (addedPoint.y < smallestYCoordinate) smallestYCoordinate = addedPoint.y
    }

    fun findNearestPointTo(otherPoint: Point) =
        (_points.minOf { it.calculateQuadraticDistanceTo(otherPoint) })

    fun addPoint(point: Point) {
        _points.add(point)
        updateExtremeCoorinatesValues(point)
    }
    fun clear() {
        _points = mutableListOf()
    }

    val points: List<Point>
        get() = _points
}