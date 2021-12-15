package points_alorithms

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point

fun convertPointFromWorldToScreenSystem(
    point: Point,
    centerPosition: Point,
    scaleCoefficient: Double,
    screenWidth: Double = 1280.0,
    screenHeight: Double = 720.0,
): Point {
    val scalingVector = Point(-screenWidth / 2.0, screenHeight / 2.0) / scaleCoefficient
    val newScreenSystemOriginToPointVector = (point - scalingVector - centerPosition) * scaleCoefficient
    return Point(newScreenSystemOriginToPointVector.x, -newScreenSystemOriginToPointVector.y)
}

fun convertPointFromScreenToWorldSystem(
    point: Point,
    centerPosition: Point,
    scaleCoefficient: Double,
    screenWidth: Double = 1280.0,
    screenHeight: Double = 720.0
): Point {
    val screenOriginToPointVector = Point(point.x, -point.y)
    val scalingVector = Point(-screenWidth / 2.0, screenHeight / 2.0)
    val centerToPointVector = screenOriginToPointVector + scalingVector
    return centerToPointVector / scaleCoefficient + centerPosition
}

fun convertLineFromWorldToScreenSystem(
    line: Line,
    centerPosition: Point,
    scaleCoefficient: Double,
    screenWidth: Double = 1280.0,
    screenHeight: Double = 720.0,
): Line = Line(line.points.map {
    convertPointFromWorldToScreenSystem(
        it,
        centerPosition,
        scaleCoefficient,
        screenWidth,
        screenHeight
    )
})

fun convertLineFromScreenToWorldSystem(
    line: Line,
    centerPosition: Point,
    scaleCoefficient: Double,
    screenWidth: Double = 1280.0,
    screenHeight: Double = 720.0,
): Line = Line(line.points.map {
    convertPointFromScreenToWorldSystem(
        it,
        centerPosition,
        scaleCoefficient,
        screenWidth,
        screenHeight
    )
})
