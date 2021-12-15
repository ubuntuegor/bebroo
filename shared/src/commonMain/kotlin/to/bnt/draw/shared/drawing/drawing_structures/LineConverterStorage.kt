package to.bnt.draw.shared.drawing.drawing_structures

import to.bnt.draw.shared.drawing.points_algorithms.Point
import to.bnt.draw.shared.drawing.points_algorithms.smoothLine

class LineConverterStorage {
    companion object {
        const val DISTRIBUTION_SEGMENTS_COUNT = 100L
        const val POINTER_AREA_EPSILON = 10.0
    }

    val simplifiedLinesStorage = LineStorage()
    var smoothedLinesStorage = LineStorage()

    var linesSortedByLeftX = arrayListOf<Pair<Long, Double>>()
    var linesSortedByRightX = arrayListOf<Pair<Long, Double>>()
    var linesSortedByTopY = arrayListOf<Pair<Long, Double>>()
    var linesSortedByDownY = arrayListOf<Pair<Long, Double>>()
    val rectanglesContainingLine = mutableMapOf<Long, Rectangle>()

    var idCounter = 0L

    fun calculateLineID() = idCounter.also { ++idCounter }

    private val lineExtremeCoordinateComparator = Comparator<Pair<Long, Double>> {
        a, b -> when {
        a.second < b.second -> -1
        a.second > b.second -> 1
        else -> 0
        }
    }

    private fun ArrayList<Pair<Long, Double>>.addByOrder(element: Pair<Long, Double>) {
        val binarySearchValue = this.binarySearch(element, lineExtremeCoordinateComparator)
        val insertionIndex = if (binarySearchValue >= 0) binarySearchValue else -(binarySearchValue + 1)
        this.add(insertionIndex, element)
    }

    private fun ArrayList<Pair<Long, Double>>.removeByOrder(element: Pair<Long, Double>): Boolean {
        val binarySearchValue = this.binarySearch(element, lineExtremeCoordinateComparator)

        if (binarySearchValue < 0) return false

        for (descendingIndex in binarySearchValue..0)
            if (this[descendingIndex].first == element.first)
                return true
        for (ascendingIndex in binarySearchValue..0)
            if (this[ascendingIndex].first == element.first)
                return true

        return false
    }

    private fun sortLineByExtremePoints(line: Line, lineID: Long) {
        val rectangleContainingLine = line.findContainingRectangle()
        linesSortedByLeftX.addByOrder(Pair(lineID, rectangleContainingLine.leftTopPoint.x))
        linesSortedByTopY.addByOrder(Pair(lineID, rectangleContainingLine.leftTopPoint.y))
        linesSortedByRightX.addByOrder(Pair(lineID, rectangleContainingLine.rightDownPoint.x))
        linesSortedByDownY.addByOrder(Pair(lineID, rectangleContainingLine.rightDownPoint.y))
        rectanglesContainingLine[lineID] = rectangleContainingLine
    }

    private fun removeLineFromSortingStorages(lineID: Long) {
        linesSortedByLeftX.removeByOrder(Pair(lineID, rectanglesContainingLine[lineID]?.leftTopPoint?.x ?: return))
        linesSortedByTopY.removeByOrder(Pair(lineID, rectanglesContainingLine[lineID]?.leftTopPoint?.y ?: return))
        linesSortedByRightX.removeByOrder(Pair(lineID, rectanglesContainingLine[lineID]?.rightDownPoint?.x ?: return))
        linesSortedByTopY.removeByOrder(Pair(lineID, rectanglesContainingLine[lineID]?.leftTopPoint?.y ?: return))
        rectanglesContainingLine.remove(lineID)
    }

    fun addLine(simplifiedLine: Line) {
        val lineID = calculateLineID()
        simplifiedLinesStorage.addLine(lineID, simplifiedLine)
        val smoothedLine = smoothLine(simplifiedLine, DISTRIBUTION_SEGMENTS_COUNT)
        smoothedLinesStorage.addLine(lineID, smoothedLine)
        sortLineByExtremePoints(smoothedLine, lineID)
    }

    fun removeLine(lineID: Long) {
        simplifiedLinesStorage.removeLine(lineID)
        smoothedLinesStorage.removeLine(lineID)
        removeLineFromSortingStorages(lineID)
    }

    fun getLineAtPoint(point: Point) {
    }

    fun getDisplayingLines() = smoothedLinesStorage.getLines()
}

