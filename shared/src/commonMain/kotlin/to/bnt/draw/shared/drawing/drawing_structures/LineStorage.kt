package to.bnt.draw.shared.drawing.drawing_structures

class LineStorage {
    private val storage = mutableMapOf<Long, Line>()

    fun addLine(lineID: Long, line: Line): Long {
        storage[lineID] = line

        return lineID
    }

    fun removeLine(lineID: Long): Line? = storage.remove(lineID)

    fun getLine(lineID: Long): Line? = storage[lineID]

    fun containsLine(lineID: Long): Boolean = storage.contains(lineID)

    fun getLines(): List<Line> = storage.values.toList()
}