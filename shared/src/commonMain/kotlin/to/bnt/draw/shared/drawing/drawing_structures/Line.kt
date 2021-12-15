package to.bnt.draw.shared.drawing.drawing_structures

data class Line(private val linePoints: List<Point> = emptyList()) {
    private var _points = linePoints.toMutableList()
    fun findContainingRectangle(): Rectangle {
        val smallestX = _points.minOf { it.x }
        val biggestX = _points.maxOf { it.x }
        val smallestY = _points.minOf { it.y }
        val biggestY = _points.maxOf { it.y }

        return Rectangle(Point(smallestX, biggestY), Point(biggestX, smallestY))
    }

    fun findNearestPointTo(otherPoint: Point) =
        (_points.minOf { it.calculateQuadraticDistanceTo(otherPoint) })

    fun addPoint(point: Point) = _points.add(point)

    fun clear() {
        _points = mutableListOf()
    }

    val points: List<Point>
        get() = _points
}