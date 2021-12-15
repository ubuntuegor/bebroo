package to.bnt.draw.shared.drawing.drawing_structures

data class Line(val points: List<Point>) {
    fun findContainingRectangle(): Rectangle {
        val smallestX = points.minOf { it.x }
        val biggestX = points.maxOf { it.x }
        val smallestY = points.minOf { it.y }
        val biggestY = points.maxOf { it.y }

        return Rectangle(Point(smallestX, biggestY), Point(biggestX, smallestY))
    }
}
