package to.bnt.draw.shared.drawing.drawing_structures

package shared.drawing.drawing_structures
import shared.drawing.line_algorithms.smoothLine
import kotlin.random.Random

class LineConverterStorage {
    companion object {
        const val DISTRIBUTION_SEGMENTS_COUNT = 100L
        const val POINTER_AREA_EPSILON = 10.0
    }

    private val simplifiedLinesStorage = LineStorage()
    private var smoothedLinesStorage = LineStorage()

    private data class LineCoordinate(val lineID: Long, val coordinateValue: Double)

    private var linesSortedByLeftX = arrayListOf<LineCoordinate>()
    private var linesSortedByRightX = arrayListOf<LineCoordinate>()
    private var linesSortedByTopY = arrayListOf<LineCoordinate>()
    private var linesSortedByDownY = arrayListOf<LineCoordinate>()
    private val rectanglesContainingLine = mutableMapOf<Long, Rectangle>()

    private var idCounter = 0L

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

