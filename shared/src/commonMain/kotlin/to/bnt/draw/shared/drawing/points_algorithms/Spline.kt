package to.bnt.draw.shared.drawing.points_algorithms

data class Spline(
    val point0: Point,
    val point1: Point,
    val point2: Point,
    val point3: Point
    ) {
    fun getPoints(): List<Point> =
        listOf(point0.copy(), point1.copy(), point2.copy(), point3.copy())
}
