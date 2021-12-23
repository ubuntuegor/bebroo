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

    private val lineExtremeCoordinateComparator = Comparator<LineCoordinate> { a, b ->
        when {
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

    private fun ArrayList<LineCoordinate>.getLinesWithCoordinateGreaterThanOrEqual(value: Double): List<Long> =
        map { it.lineID }
            .slice(smallestIndexForCoordinateGreaterThanOrEqual(value)..lastIndex)

    private fun ArrayList<LineCoordinate>.getLinesWithCoordinateLessThanOrEqual(value: Double): List<Long> =
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

    fun getLinesWithIntersectingRectangles(intersectedRectangle: Rectangle): Set<Long> {
        val linesByLeftX =
            linesSortedByLeftX.getLinesWithCoordinateLessThanOrEqual(intersectedRectangle.rightDownPoint.x)
        val linesByRightX =
            linesSortedByRightX.getLinesWithCoordinateGreaterThanOrEqual(intersectedRectangle.leftTopPoint.x)
        val linesByTopY =
            linesSortedByTopY.getLinesWithCoordinateGreaterThanOrEqual(intersectedRectangle.rightDownPoint.y)
        val linesByDownY =
            linesSortedByDownY.getLinesWithCoordinateLessThanOrEqual(intersectedRectangle.leftTopPoint.y)

        return linesByLeftX.toSet()
            .intersect(linesByRightX.toSet())
            .intersect(linesByTopY.toSet())
            .intersect(linesByDownY.toSet())
    }
}

data class AddLineResult(
    val id: Long,
    val worldSimplifiedLine: Line,
    val screenSmoothedLine: Line
)

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
            screenWidth.toDouble(),
            screenHeight.toDouble(),
            scaleCoefficient
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
            cameraPoint,
            screenWidth.toDouble(),
            screenHeight.toDouble(),
            scaleCoefficient
        )

    fun addLine(simplifiedLine: Line): AddLineResult? {
        if (simplifiedLine.points.size < 2) return null
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
            smoothedLineInWorldSystem.containingRectangle ?: return null
        )
        linesID.add(lineID)

        return AddLineResult(
            lineID,
            simplifiedLineInWorldSystem,
            convertLineFromWorldToScreenSystem(smoothedLineInWorldSystem)
        )
    }

    fun addWorldLine(simplifiedLine: Line): AddLineResult? {
        val lineID = calculateLineID()
        simplifiedLinesStorage.addLine(lineID, simplifiedLine)
        val smoothedLineInWorldSystem = addLineWithSmooth(lineID, simplifiedLine)
        rectanglesStorage.addRectangle(
            lineID,
            smoothedLineInWorldSystem.containingRectangle ?: return null
        )
        linesID.add(lineID)

        return AddLineResult(
            lineID,
            simplifiedLine,
            convertLineFromWorldToScreenSystem(smoothedLineInWorldSystem)
        )
    }

    fun removeLine(lineID: Long) {
        simplifiedLinesStorage.removeLine(lineID)
        smoothedLinesStorage.removeLine(lineID)
        rectanglesStorage.removeRectangleByLineID(lineID)
        linesID.remove(lineID)
    }

    private fun findPointerAreaSideLength() = POINTER_AREA_EPSILON / scaleCoefficient

    private fun getNearestLineAtPointerArea(suspectedLinesID: Set<Long>, point: Point): Long? {
        var nearestLineID: Long? = null
        var minDistance = Double.POSITIVE_INFINITY
        suspectedLinesID.forEach {
            val distance = smoothedLinesStorage.getLine(it)?.findNearestPointTo(point) ?: Double.POSITIVE_INFINITY
            if (distance <= findPointerAreaSideLength() && distance < minDistance) {
                nearestLineID = it
                minDistance = distance
            }
        }

        return nearestLineID
    }

    fun getLineAtPoint(point: Point): Long? {
        val pointInWorldSystem = this.convertPointFromScreenToWorldSystem(point)
        val pointerAreaSideLength = findPointerAreaSideLength()
        val pointRectangle = Rectangle(
            Point(pointInWorldSystem.x - pointerAreaSideLength, pointInWorldSystem.y + pointerAreaSideLength),
            Point(pointInWorldSystem.x + pointerAreaSideLength, pointInWorldSystem.y - pointerAreaSideLength)
        )

        val suspectedLinesID = rectanglesStorage.getLinesWithIntersectingRectangles(pointRectangle)
        return getNearestLineAtPointerArea(suspectedLinesID, pointInWorldSystem)
    }

    fun removeLineAtPoint(point: Point): Long? {
        val removedLineID = getLineAtPoint(point)
        removedLineID ?: return null
        removeLine(removedLineID)
        rectanglesStorage.removeRectangleByLineID(removedLineID)
        return removedLineID
    }

    private fun convertDisplayingLinesInScreenSystem(): List<Line> {
        return smoothedLinesStorage.lines.map { convertLineFromWorldToScreenSystem(it) }
    }

    val displayingLines: List<Line>
        get() = convertDisplayingLinesInScreenSystem()

    fun changeScaleCoefficient(newScaleCoefficient: Double) {
        if (newScaleCoefficient <= 0.0) return
        scaleCoefficient = newScaleCoefficient.coerceIn(MIN_SCALE_VALUE, MAX_SCALE_VALUE)
    }

    fun changeScreenSize(newWidth: Int, newHeight: Int) {
        screenWidth = newWidth
        screenHeight = newHeight
    }

    fun translateCamera(translationVector: Point) {
        cameraPoint += translationVector / scaleCoefficient
    }

    companion object {
        const val DEFAULT_DISTRIBUTION_SEGMENTS_COUNT = 15L
        const val POINTER_AREA_EPSILON = 50.0
        const val MIN_SCALE_VALUE = 0.01
        const val MAX_SCALE_VALUE = 100.0
        const val MAX_DISTRIBUTION_SEGMENTS_COUNT = 30L
        const val MIN_DISTRIBUTION_SEGMENTS_COUNT = 5L
    }
}
