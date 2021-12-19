package to.bnt.draw.shared.drawing.drawing_structures

data class Spline(
    val point0: Point,
    val point1: Point,
    val point2: Point,
    val point3: Point
    ) {
    fun getPoints(): List<Point> =
        listOf(point0, point1, point2, point3)
}
