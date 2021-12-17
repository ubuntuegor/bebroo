package to.bnt.draw.shared.drawing.line_algorithms

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point
import kotlin.math.abs
import kotlin.math.sqrt

private fun perpendicularDistance(lineFirstPoint: Point, lineSecondPoint: Point, point: Point): Double {
    val a = lineSecondPoint.y - lineFirstPoint.y
    val b = -(lineSecondPoint.x - lineFirstPoint.x)
    val c = -lineFirstPoint.x * a - lineFirstPoint.y * b

    return abs(a * point.x + b * point.y + c) / sqrt(a*a + b*b)
}

fun douglasPeuckerAlgorithm(line: Line, epsilon: Double): Line {
    val points = line.points
    val indicesStack = ArrayDeque<Pair<Int, Int>>()
    val keepPoint: Array<Boolean> = Array(points.size) { true }
    indicesStack.add(Pair(0, points.lastIndex))

    while (!indicesStack.isEmpty()) {
        val (startIndex, endIndex) = indicesStack.first()
        indicesStack.removeFirst()

        var maxDistance = 0.0
        var index = startIndex
        for (i in (startIndex + 1) until endIndex) {
            if (keepPoint[i]) {
                val distance = perpendicularDistance(points[startIndex], points[endIndex], points[i])
                if (distance > maxDistance) {
                    index = i
                    maxDistance = distance
                }
            }
        }

        if (maxDistance >= epsilon) {
            indicesStack.add(Pair(startIndex, index))
            indicesStack.add(Pair(index, endIndex))
        } else {
            for (i in startIndex + 1 until endIndex)
                keepPoint[i] = false
        }
    }

    return Line(points.filterIndexed { index, _ -> keepPoint[index] })
}

// Calculates a simplified line with fewer points.
// The accuracy not worse than epsilon.
fun simplifyLine(line: Line, epsilon: Double): Line {
    return douglasPeuckerAlgorithm(line, epsilon)
}
