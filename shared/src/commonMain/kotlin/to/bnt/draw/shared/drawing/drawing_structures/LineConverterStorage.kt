package to.bnt.draw.shared.drawing.drawing_structures

package shared.drawing.drawing_structures
import shared.drawing.line_algorithms.smoothLine
import kotlin.random.Random

class LineConverterStorage {
    companion object {
        const val DEFAULT_DISTRIBUTION_SEGMENTS_COUNT = 100L
        const val POINTER_AREA_EPSILON = 10.0
    }

    private data class LineCoordinate(val lineID: Long, val coordinateValue: Double)

    private val simplifiedLinesStorage = LineStorage()
    private var smoothedLinesStorage = LineStorage()
    private val linesID = mutableSetOf<Long>()

    private var linesSortedByLeftX = arrayListOf<LineCoordinate>()
    private var linesSortedByRightX = arrayListOf<LineCoordinate>()
    private var linesSortedByTopY = arrayListOf<LineCoordinate>()
    private var linesSortedByDownY = arrayListOf<LineCoordinate>()
    private val rectanglesContainingLine = mutableMapOf<Long, Rectangle>()

    private var idCounter = 0L

    private var scaleCoefficient: Double = 1.0

    private fun calculateLineID() = idCounter.also { ++idCounter }

    private val lineExtremeCoordinateComparator = Comparator<LineCoordinate> {
            a, b -> when {
        a.coordinateValue < b.coordinateValue -> -1
        a.coordinateValue > b.coordinateValue -> 1
        else -> 0
    }
    }

    private fun ArrayList<LineCoordinate>.findInsertionIndexByOrder(lineCoordinate: LineCoordinate): Int {
        val binarySearchValue = this.binarySearch(lineCoordinate, lineExtremeCoordinateComparator)
        return if (binarySearchValue >= 0) binarySearchValue else -(binarySearchValue + 1)
    }

    private fun ArrayList<LineCoordinate>.addByOrder(lineCoordinate: LineCoordinate) {
        this.add(findInsertionIndexByOrder(lineCoordinate), lineCoordinate)
    }

    private fun ArrayList<LineCoordinate>.removeByOrder(lineCoordinate: LineCoordinate): Boolean {
        val insertionIndex = findInsertionIndexByOrder(lineCoordinate)
        if (insertionIndex !in indices) return false

        for (descendingIndex in insertionIndex downTo 0)
            if (this[descendingIndex].lineID == lineCoordinate.lineID) {
                this.removeAt(descendingIndex)
                return true
            }
        for (ascendingIndex in (insertionIndex + 1)..lastIndex)
            if (this[ascendingIndex].lineID == lineCoordinate.lineID) {
                removeAt(ascendingIndex)
                return true
            }

        return false
    }

    private fun sortLineByExtremePoints(line: Line, lineID: Long) {
        val rectangleContainingLine = line.findContainingRectangle()
        linesSortedByLeftX.addByOrder(LineCoordinate(lineID, rectangleContainingLine.leftTopPoint.x))
        linesSortedByTopY.addByOrder(LineCoordinate(lineID, rectangleContainingLine.leftTopPoint.y))
        linesSortedByRightX.addByOrder(LineCoordinate(lineID, rectangleContainingLine.rightDownPoint.x))
        linesSortedByDownY.addByOrder(LineCoordinate(lineID, rectangleContainingLine.rightDownPoint.y))
        rectanglesContainingLine[lineID] = rectangleContainingLine
    }

    private fun removeLineFromSortingStorages(lineID: Long) {
        linesSortedByLeftX.removeByOrder(
            LineCoordinate(lineID, rectanglesContainingLine[lineID]?.leftTopPoint?.x ?: return)
        )
        linesSortedByTopY.removeByOrder(
            LineCoordinate(lineID, rectanglesContainingLine[lineID]?.leftTopPoint?.y ?: return)
        )
        linesSortedByRightX.removeByOrder(
            LineCoordinate(lineID, rectanglesContainingLine[lineID]?.rightDownPoint?.x ?: return)
        )
        linesSortedByDownY.removeByOrder(
            LineCoordinate(lineID, rectanglesContainingLine[lineID]?.rightDownPoint?.y ?: return)
        )
        rectanglesContainingLine.remove(lineID)
    }

    private fun findDistributionSegmentsCount(): Long {
        var segmentsCount = (DEFAULT_DISTRIBUTION_SEGMENTS_COUNT * scaleCoefficient).toLong()
        if (segmentsCount == 0L) segmentsCount = 1L
        return segmentsCount
    }

    private fun addSmoothedLine(lineID: Long, simplifiedLine: Line): Line {
        val smoothedLine =
            smoothLine(simplifiedLine, findDistributionSegmentsCount())
        return smoothedLinesStorage.addLine(lineID, smoothedLine)
    }

    private fun recalculateSmoothedLines() {
        smoothedLinesStorage = LineStorage()
        for (lineID in linesID) {
            val simplifiedLine = simplifiedLinesStorage.getLine(lineID)
            simplifiedLine ?: continue
            addSmoothedLine(lineID, simplifiedLine)
        }
    }

    fun addLine(simplifiedLine: Line) {
        val lineID = calculateLineID()
        simplifiedLinesStorage.addLine(lineID, simplifiedLine)
        val smoothedLine = addSmoothedLine(lineID, simplifiedLine)
        sortLineByExtremePoints(smoothedLine, lineID)
        linesID.add(lineID)
    }

    fun addAll(lines: List<Line>) = lines.forEach { addLine(it) }

    fun removeLine(lineID: Long) {
        simplifiedLinesStorage.removeLine(lineID)
        smoothedLinesStorage.removeLine(lineID)
        removeLineFromSortingStorages(lineID)
        linesID.remove(lineID)
    }

    private fun ArrayList<LineCoordinate>.smallestIndexForCoordinateGreaterThanOrEqual(value: Double): Int {
        var smallestSuitableLineIndex = findInsertionIndexByOrder(LineCoordinate(0L, value))
        while (smallestSuitableLineIndex >= 1) {
            if (this[smallestSuitableLineIndex - 1].coordinateValue < value)
                break
            --smallestSuitableLineIndex
        }
        return smallestSuitableLineIndex
    }

    private fun ArrayList<LineCoordinate>.biggestIndexForCoordinateLessThanOrEqual(value: Double): Int {
        var biggestSuitableLineIndex = findInsertionIndexByOrder(LineConverterStorage.LineCoordinate(0L, value))
        --biggestSuitableLineIndex;
        while (biggestSuitableLineIndex < lastIndex) {
            if (this[biggestSuitableLineIndex + 1].coordinateValue > value)
                break
            ++biggestSuitableLineIndex
        }
        return biggestSuitableLineIndex
    }

    private fun ArrayList<LineCoordinate>.getLinesWithCoordinateGreaterThanOrEqual(value: Double): List<Long> =
        map { it.lineID }.slice(smallestIndexForCoordinateGreaterThanOrEqual(value)..lastIndex)

    private fun ArrayList<LineCoordinate>.getLinesWithCoordinateLessThanOrEqual(value: Double): List<Long> =
        map { it.lineID }.slice(0..biggestIndexForCoordinateLessThanOrEqual(value))

    private fun ArrayList<LineConverterStorage.LineCoordinate>.getLinesWithCoordinateInSegment(
        leftBorderValue: Double,
        rightBorderValue: Double
    ): List<Long> {
        if (leftBorderValue > rightBorderValue) return emptyList()
        val smallestSuitableLineIndex = smallestIndexForCoordinateGreaterThanOrEqual(leftBorderValue)
        val biggestSuitableLineIndex = biggestIndexForCoordinateLessThanOrEqual(rightBorderValue)
        if (smallestSuitableLineIndex == biggestSuitableLineIndex) return listOf(this[smallestSuitableLineIndex].lineID)
        return this.map { it.lineID }.slice(smallestSuitableLineIndex..biggestSuitableLineIndex)
    }

    private fun getLinesWithContainingRectangles(innerRectangle: Rectangle): Set<Long> {
        val linesByLeftX = linesSortedByLeftX.getLinesWithCoordinateLessThanOrEqual(innerRectangle.leftTopPoint.x)
        val linesByRightX = linesSortedByRightX.getLinesWithCoordinateGreaterThanOrEqual(innerRectangle.rightDownPoint.x,)
        val linesByTopY = linesSortedByTopY.getLinesWithCoordinateGreaterThanOrEqual(innerRectangle.leftTopPoint.y)
        val linesByDownY = linesSortedByDownY.getLinesWithCoordinateLessThanOrEqual(innerRectangle.rightDownPoint.y)

        return linesByLeftX.intersect(linesByRightX).intersect(linesByTopY).intersect(linesByDownY)
    }

    private fun getLinesWithIntersectingRectangles(intersectedRectangle: Rectangle): Set<Long> {
        val linesByLeftX = linesSortedByLeftX.getLinesWithCoordinateInSegment(
            intersectedRectangle.leftTopPoint.x, intersectedRectangle.rightDownPoint.x
        )
        val linesByRightX = linesSortedByRightX.getLinesWithCoordinateInSegment(
            intersectedRectangle.leftTopPoint.x, intersectedRectangle.rightDownPoint.x
        )
        val linesByTopY = linesSortedByTopY.getLinesWithCoordinateInSegment(
            intersectedRectangle.rightDownPoint.y, intersectedRectangle.leftTopPoint.y
        )
        val linesByDownY = linesSortedByDownY.getLinesWithCoordinateInSegment(
            intersectedRectangle.rightDownPoint.y, intersectedRectangle.leftTopPoint.y
        )

        return linesByLeftX.union(linesByRightX).filter { linesByDownY.union(linesByTopY).contains(it) }.toSet()
    }

    private fun getRectanglesWithCommonPart(givenRectangle: Rectangle): Set<Long> {
        return getLinesWithIntersectingRectangles(givenRectangle) +
                getLinesWithContainingRectangles(givenRectangle)
    }

    fun getLineAtPoint(point: Point): Line? {
        val areaEpsilonVector = Point(POINTER_AREA_EPSILON, POINTER_AREA_EPSILON)
        var pointRectangle = Rectangle(
            point - areaEpsilonVector,
            point + areaEpsilonVector
        )

        val suspectedLinesID = getRectanglesWithCommonPart(pointRectangle)
        val suspectedLines = suspectedLinesID.map { smoothedLinesStorage.getLine(it) }
        return suspectedLines.minByOrNull { it?.findNearestPointTo(point) ?: Double.POSITIVE_INFINITY }
    }

    fun getDisplayingLines() = smoothedLinesStorage.getLines()

    fun changeScaleCoefficient(newScaleCoefficient: Double) {
        if (newScaleCoefficient <= 0.0) return
        scaleCoefficient = newScaleCoefficient
        recalculateSmoothedLines()
    }
}
