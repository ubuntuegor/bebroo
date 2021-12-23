package to.bnt.draw.shared.drawing.points_algorithms

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.drawing_structures.Spline
import kotlin.math.abs
import kotlin.math.pow

const val ALPHA_COEFFICIENT = 0.5

private fun sqr(number: Double) = number * number

private fun calculateTCoefficient(
    previousTCoefficient: Double,
    previousPoint: Point,
    currentPoint: Point
): Double {
    val (previousX, previousY) = previousPoint.coordinates
    val (currentX, currentY) = currentPoint.coordinates
    return (sqr(currentX - previousX) + sqr(currentY - previousY))
        .pow(ALPHA_COEFFICIENT / 2.0) + previousTCoefficient
}

private fun calculateLetterPoint(firstT: Double,
                                 secondT: Double,
                                 firstPoint: Point,
                                 secondPoint: Point,
                                 tCoefficient: Double
) = Point(
    (secondT - tCoefficient) / (secondT - firstT) * firstPoint.x +
            (tCoefficient - firstT) / (secondT - firstT) * secondPoint.x,
    (secondT - tCoefficient) / (secondT - firstT) * firstPoint.y +
            (tCoefficient - firstT) / (secondT - firstT) * secondPoint.y,
)

private fun calculateCatmullRomPoint(
    tList: List<Double>,
    points: List<Point>,
    tCoefficient: Double
): Point {
    val aPointsList = mutableListOf<Point>()
    for (i in 0..2) {
        aPointsList.add(calculateLetterPoint(
            tList[i],
            tList[i + 1],
            points[i],
            points[i + 1],
            tCoefficient
        ))
    }
    val bPointsList = mutableListOf<Point>()
    for (i in 0..1) {
        bPointsList.add(calculateLetterPoint(
            tList[i],
            tList[i + 2],
            aPointsList[i],
            aPointsList[i + 1],
            tCoefficient
        ))
    }

    return calculateLetterPoint(tList[1], tList[2], bPointsList[0], bPointsList[1], tCoefficient)
}

private fun calculateIntervalDistribution(firstPoint: Double, secondPoint: Double, segmentsCount: Long) : List<Double> {
    val intervalDistribution = mutableListOf<Double>()
    val lessPoint = if (firstPoint <= secondPoint) firstPoint else secondPoint
    val intervalLength = abs(secondPoint - firstPoint)
    for (i in 0..segmentsCount) {
        intervalDistribution.add(lessPoint + intervalLength * i.toDouble() / segmentsCount.toDouble())
    }

    return intervalDistribution
}

private fun calculateCatmullRomSplinePoints(spline: Spline, distributionSegmentsCount: Long): List<Point> {
    val points = spline.points
    val tList = mutableListOf(0.0)
    for (i in 0..2) {
        tList.add(calculateTCoefficient(tList.last(), points[i], points[i + 1]))
    }

    val splinePoints = mutableListOf<Point>()
    val intervalDistribution = calculateIntervalDistribution(tList[1], tList[2],  distributionSegmentsCount)
    intervalDistribution.forEach { tCoefficient -> splinePoints.add(
        calculateCatmullRomPoint(tList, points, tCoefficient)
    ) }

    return splinePoints
}

private fun calculateCatmullRomCurvePoints(pivotPointsLine: Line, distributionSegmentsCount: Long): Line {
    val pivotPoints = pivotPointsLine.points
    val chainPoints = mutableListOf<Point>()
    for (i in 0 until (pivotPoints.count() - 3)) {
        val currentSpline = Spline(
            pivotPoints[i],
            pivotPoints[i + 1],
            pivotPoints[i + 2],
            pivotPoints[i + 3]
        )
        chainPoints.addAll(calculateCatmullRomSplinePoints(currentSpline, distributionSegmentsCount))
    }

    return pivotPointsLine.copy(linePoints = chainPoints)
}

private fun findContinuationPoint(fromPoint: Point, toPoint: Point) = toPoint + fromPoint.getVectorTo(toPoint)

// Calculates a smoothed line through the pivot points with given segments distribution count.
fun smoothLine(pivotPointsLine: Line, distributionSegmentsCount: Long): Line {
    val pivotPoints = pivotPointsLine.points
    if (pivotPoints.count() < 2) return pivotPointsLine

    val firstPoint = pivotPoints.first()
    val secondPoint = pivotPoints[1]
    val preLastPoint = pivotPoints[pivotPoints.lastIndex - 1]
    val lastPoint = pivotPoints.last()

    val firstPointContinuation = findContinuationPoint(secondPoint, firstPoint)
    val lastPointContinuation = findContinuationPoint(preLastPoint, lastPoint)
    val augmentedPivotPoints = pivotPoints.toMutableList()
    augmentedPivotPoints.add(0, firstPointContinuation)
    augmentedPivotPoints.add(lastPointContinuation)

    return calculateCatmullRomCurvePoints(pivotPointsLine.copy(linePoints = augmentedPivotPoints), distributionSegmentsCount)
}
