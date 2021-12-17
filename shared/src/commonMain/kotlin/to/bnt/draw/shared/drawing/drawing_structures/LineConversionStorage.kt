package to.bnt.draw.shared.drawing.drawing_structures

import to.bnt.draw.shared.drawing.line_algorithms.convertLineFromScreenToWorldSystem
import to.bnt.draw.shared.drawing.line_algorithms.convertLineFromWorldToScreenSystem
import to.bnt.draw.shared.drawing.line_algorithms.convertPointFromScreenToWorldSystem
import to.bnt.draw.shared.drawing.points_algorithms.smoothLine

private data class LineCoordinate(val lineID: Long, val coordinateValue: Double)


private class LineRectanglesStorage {
    private var linesSortedByLeftX = arrayListOf<LineCoordinate>()
    private var linesSortedByRightX = arrayListOf<LineCoordinate>()
    private var linesSortedByTopY = arrayListOf<LineCoordinate>()
    private var linesSortedByDownY = arrayListOf<LineCoordinate>()

    private val rectanglesContainingLine = mutableMapOf<Long, Rectangle>()

    private val lineExtremeCoordinateComparator = Comparator<LineCoordinate> {
            a, b -> when {
        a.coordinateValue < b.coordinateValue -> -1
        a.coordinateValue > b.coordinateValue -> 1
        else -> 0
    }
    }

    fun addRectangle(lineID: Long, rectangle: Rectangle) {
        rectanglesContainingLine[lineID] = rectangle
        sortRectangleByCornerPoints(lineID, rectangle)
    }

    fun removeRectangleByLineID(lineID: Long): Rectangle? {
        removeLineFromSortingStorages(lineID)
        return rectanglesContainingLine.remove(lineID)
    }

    private fun ArrayList<LineCoordinate>
            .findInsertionIndexByOrder(lineCoordinate: LineCoordinate): Int {
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

    private fun ArrayList<LineCoordinate>
            .smallestIndexForCoordinateGreaterThanOrEqual(value: Double): Int {
        var smallestSuitableLineIndex = findInsertionIndexByOrder(LineCoordinate(0L, value))
        while (smallestSuitableLineIndex >= 1) {
            if (this[smallestSuitableLineIndex - 1].coordinateValue < value)
                break
            --smallestSuitableLineIndex
        }
        return smallestSuitableLineIndex
    }

    private fun ArrayList<LineCoordinate>
            .biggestIndexForCoordinateLessThanOrEqual(value: Double): Int {
        var biggestSuitableLineIndex = findInsertionIndexByOrder(LineCoordinate(0L, value))
        --biggestSuitableLineIndex
        while (biggestSuitableLineIndex < lastIndex) {
            if (this[biggestSuitableLineIndex + 1].coordinateValue > value)
                break
            ++biggestSuitableLineIndex
        }
        return biggestSuitableLineIndex
    }

    private fun sortRectangleByCornerPoints(lineID: Long, rectangle: Rectangle) {
        linesSortedByLeftX.addByOrder(LineCoordinate(lineID, rectangle.leftTopPoint.x))
        linesSortedByTopY.addByOrder(LineCoordinate(lineID, rectangle.leftTopPoint.y))
        linesSortedByRightX.addByOrder(LineCoordinate(lineID, rectangle.rightDownPoint.x))
        linesSortedByDownY.addByOrder(LineCoordinate(lineID, rectangle.rightDownPoint.y))
        rectanglesContainingLine[lineID] = rectangle
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

    private fun ArrayList<LineCoordinate>.
            getLinesWithCoordinateGreaterThanOrEqual(value: Double): List<Long> =
        map { it.lineID }
            .slice(smallestIndexForCoordinateGreaterThanOrEqual(value)..lastIndex)

    private fun ArrayList<LineCoordinate>.
            getLinesWithCoordinateLessThanOrEqual(value: Double): List<Long> =
        map { it.lineID }
            .slice(0..biggestIndexForCoordinateLessThanOrEqual(value))

    private fun ArrayList<LineCoordinate>.getLinesWithCoordinateInSegment(
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
        val linesByLeftX =
            linesSortedByLeftX.getLinesWithCoordinateLessThanOrEqual(innerRectangle.leftTopPoint.x)
        val linesByRightX =
            linesSortedByRightX.getLinesWithCoordinateGreaterThanOrEqual(innerRectangle.rightDownPoint.x)
        val linesByTopY =
            linesSortedByTopY.getLinesWithCoordinateGreaterThanOrEqual(innerRectangle.leftTopPoint.y)
        val linesByDownY =
            linesSortedByDownY.getLinesWithCoordinateLessThanOrEqual(innerRectangle.rightDownPoint.y)

        return linesByLeftX
            .intersect(linesByRightX.toSet())
            .intersect(linesByTopY.toSet())
            .intersect(linesByDownY.toSet())
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

    fun getRectanglesWithCommonPart(givenRectangle: Rectangle): Set<Long> {
        return getLinesWithIntersectingRectangles(givenRectangle) +
                getLinesWithContainingRectangles(givenRectangle)
    }
}

class LineConversionStorage(private var screenWidth: Int, private var screenHeight: Int) {
    private val simplifiedLinesStorage = LineStorage()
    private var smoothedLinesStorage = LineStorage()
    private val linesID = mutableSetOf<Long>()
    private val rectanglesStorage = LineRectanglesStorage()

    private var idCounter = 0L

    private var scaleCoefficient: Double = 1.0
    private var cameraPoint = Point(0.0, 0.0)

    private fun calculateLineID() = idCounter.also { ++idCounter }

    private fun calculateDistributionSegmentsCount(): Long {
        val count = (DEFAULT_DISTRIBUTION_SEGMENTS_COUNT / scaleCoefficient).toLong()
        return when {
            count > MAX_DISTRIBUTION_SEGMENTS_COUNT -> MAX_DISTRIBUTION_SEGMENTS_COUNT
            count < MIN_DISTRIBUTION_SEGMENTS_COUNT -> MIN_DISTRIBUTION_SEGMENTS_COUNT
            else -> count
        }
    }

    private fun addLineWithSmooth(lineID: Long, simplifiedLine: Line): Line {
        val smoothedLine =
            smoothLine(simplifiedLine, calculateDistributionSegmentsCount())
        return smoothedLinesStorage.addLine(lineID, smoothedLine)
    }

    private fun convertPointFromScreenToWorldSystem(point: Point) =
        convertPointFromScreenToWorldSystem(
            point,
            cameraPoint,
            scaleCoefficient,
            screenWidth.toDouble(),
            screenHeight.toDouble()
        )

    private fun convertLineFromScreenToWorldSystem(line: Line) =
        convertLineFromScreenToWorldSystem(
            line,
            cameraPoint,
            screenWidth.toDouble(),
            screenHeight.toDouble(),
            scaleCoefficient
        )

    private fun convertLineFromWorldToScreenSystem(line: Line) =
        convertLineFromWorldToScreenSystem(
            line,
            cameraPoint + Point(200.0, 200.0),
            screenWidth.toDouble(),
            screenHeight.toDouble(),
            scaleCoefficient
        )

    fun addLine(simplifiedLine: Line): Line? {
        val lineID = calculateLineID()
        val simplifiedLineInWorldSystem =
            simplifiedLinesStorage.addLine(
                lineID,
                this.convertLineFromScreenToWorldSystem(simplifiedLine)
            )
        val smoothedLineInWorldSystem = addLineWithSmooth(
            lineID,
            simplifiedLineInWorldSystem
        )
        rectanglesStorage.addRectangle(
            lineID,
            smoothedLineInWorldSystem.getContainingRectangle() ?: return null
        )
        linesID.add(lineID)

        return convertLineFromWorldToScreenSystem(smoothedLineInWorldSystem)
    }

    fun addAll(lines: List<Line>) = lines.forEach { addLine(it) }

    private fun removeLine(lineID: Long) {
        simplifiedLinesStorage.removeLine(lineID)
        smoothedLinesStorage.removeLine(lineID)
        rectanglesStorage.removeRectangleByLineID(lineID)
        linesID.remove(lineID)
    }

    private fun getLineAtPoint(point: Point): Long? {
        val pointInWorldSystem = this.convertPointFromScreenToWorldSystem(point)
        val areaEpsilonVector = Point(POINTER_AREA_EPSILON, POINTER_AREA_EPSILON)
        val pointRectangle = Rectangle(
            pointInWorldSystem - areaEpsilonVector,
            pointInWorldSystem + areaEpsilonVector
        )

        val suspectedLinesID = rectanglesStorage.getRectanglesWithCommonPart(pointRectangle)
        return suspectedLinesID.minByOrNull {
            smoothedLinesStorage.getLine(it)?.findNearestPointTo(point) ?: Double.POSITIVE_INFINITY
        }
    }

    fun removeLineAtPoint(point: Point) {
        val removedLineID = getLineAtPoint(point)
        removedLineID ?: return
        removeLine(removedLineID)
        rectanglesStorage.removeRectangleByLineID(removedLineID)
    }

    private fun convertDisplayingLinesInScreenSystem(): List<Line> {
        return smoothedLinesStorage.getLines().map { convertLineFromWorldToScreenSystem(it) }
    }

    fun getDisplayingLines(): List<Line> {
        return convertDisplayingLinesInScreenSystem()
    }

    fun changeScaleCoefficient(newScaleCoefficient: Double) {
        if (newScaleCoefficient <= 0.0) return
        scaleCoefficient = newScaleCoefficient.coerceIn(MIN_SCALE_VALUE, MAX_SCALE_VALUE)
    }

    fun changeScreenSize(newWidth: Int, newHeight: Int) {
        screenWidth = newWidth
        screenHeight = newHeight
    }

    fun translateCamera(translationVector: Point) {
        println(cameraPoint)
        cameraPoint += translationVector
    }

    companion object {
        const val DEFAULT_DISTRIBUTION_SEGMENTS_COUNT = 15L
        const val POINTER_AREA_EPSILON = 10.0
        const val MIN_SCALE_VALUE = 0.01
        const val MAX_SCALE_VALUE = 100.0
        const val MAX_DISTRIBUTION_SEGMENTS_COUNT = 30L
        const val MIN_DISTRIBUTION_SEGMENTS_COUNT = 5L
    }
}
