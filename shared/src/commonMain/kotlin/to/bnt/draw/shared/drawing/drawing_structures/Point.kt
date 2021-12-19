package to.bnt.draw.shared.drawing.drawing_structures

data class Point(var x: Double, var y: Double) {
    operator fun plus(otherPoint: Point) = Point(x + otherPoint.x, y + otherPoint.y)

    operator fun unaryMinus() = Point(-x, -y)

    operator fun minus(otherPoint: Point) = plus(-otherPoint)

    operator fun times(number: Double) = Point(x * number, y * number)

    operator fun div(number: Double) = Point(x / number, y / number)

    fun getCoordinates(): Pair<Double, Double> = Pair(x, y)

    fun getVectorTo(toPoint: Point) = toPoint - this

    fun calculateQuadraticDistanceTo(otherPoint: Point) =
        (otherPoint.x - x) * (otherPoint.x - x) + (otherPoint.y - y) * (otherPoint.y - y)
}
