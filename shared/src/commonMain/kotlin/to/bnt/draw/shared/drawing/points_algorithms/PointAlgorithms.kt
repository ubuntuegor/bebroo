package to.bnt.draw.shared.drawing.points_algorithms

import kotlin.math.abs
import kotlin.math.pow

const val ALPHA_COEFFICIENT = 0.5

private fun sqr(number: Double) = number * number

private fun calculateTCoefficient(
    previousTCoefficient: Double,
    previousPoint: Point,
    currentPoint: Point
): Double {
    val (previousX, previousY) = previousPoint.getCoordinates()
    val (currentX, currentY) = currentPoint.getCoordinates()
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

private fun calculateCatmullRollPoint(
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

private fun calculateCatmullRollSplinePoints(spline: Spline, distributionSegmentsCount: Long): List<Point> {
    val points = spline.getPoints()
    val tList = mutableListOf(0.0)
    for (i in 0..2) {
        tList.add(calculateTCoefficient(tList.last(), points[i], points[i + 1]))
    }

    val splinePoints = mutableListOf<Point>()
    val intervalDistribution = calculateIntervalDistribution(tList[1], tList[2],  distributionSegmentsCount)
    intervalDistribution.forEach { tCoefficient -> splinePoints.add(
        calculateCatmullRollPoint(tList, points, tCoefficient)
    ) }

    return splinePoints
}

private fun calculateCatmullRollCurvePoints(pivotPoints: List<Point>, distributionSegmentsCount: Long): List<Point> {
    val chainPoints = mutableListOf<Point>()
    for (i in 0 until (pivotPoints.count() - 3)) {
        val currentSpline = Spline(
            pivotPoints[i],
            pivotPoints[i + 1],
            pivotPoints[i + 2],
            pivotPoints[i + 3]
        )
        chainPoints.addAll(calculateCatmullRollSplinePoints(currentSpline, distributionSegmentsCount))
    }

    return chainPoints
}

private fun findContinuationPoint(fromPoint: Point, toPoint: Point) = toPoint + fromPoint.getVectorTo(toPoint)

fun calculateCurvePoints(pivotPoints: List<Point>, distributionSegmentsCount: Long): List<Point> {
    if (pivotPoints.count() < 2) return emptyList()

    val firstPoint = pivotPoints.first()
    val secondPoint = pivotPoints[1]
    val preLastPoint = pivotPoints[pivotPoints.lastIndex - 1]
    val lastPoint = pivotPoints.last()

    val firstPointContinuation = findContinuationPoint(secondPoint, firstPoint)
    val lastPointContinuation = findContinuationPoint(preLastPoint, lastPoint)
    val augmentedPivotPoints = pivotPoints.toMutableList()
    augmentedPivotPoints.add(0, firstPointContinuation)
    augmentedPivotPoints.add(lastPointContinuation)

    return calculateCatmullRollCurvePoints(augmentedPivotPoints, distributionSegmentsCount)
}
