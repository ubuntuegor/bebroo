package to.bnt.draw.shared.drawing.points_algorithms

data class Point(val x: Double, val y: Double) {
    fun getCoordinates(): Pair<Double, Double> = Pair(x, y)
}