package points_alorithms

import to.bnt.draw.shared.drawing.points_algorithms.Point

fun convertFromWorldToScreenSystem(
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

fun convertFromScreenToWorldSystem(
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
