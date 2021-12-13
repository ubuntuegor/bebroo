package to.bnt.draw.shared.drawing.points_algorithms

data class Point(val x: Double, val y: Double) {
    operator fun plus(otherPoint: Point) = Point(x + otherPoint.x, y + otherPoint.y)
    operator fun unaryMinus() = Point(-x, -y)
    operator fun minus(otherPoint: Point) = plus(-otherPoint)

    fun getCoordinates(): Pair<Double, Double> = Pair(x, y)
    fun getVectorTo(toPoint: Point) = toPoint - this
}
