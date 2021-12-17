package to.bnt.draw.shared.drawing.line_algorithms

import shared.drawing.drawing_structures.Point
import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point

fun scalePoint(point: Point, centerPoint: Point, scaleCoefficient: Double): Point =
    point * scaleCoefficient + centerPoint * (1.0 - scaleCoefficient)

fun convertPointToAnotherSystemWithoutScaling(point: Point, newOriginPoint: Point): Point {
    return (point - newOriginPoint).apply { y = -y }
}

fun convertPointFromScreenToWorldSystem(
    point: Point,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Point {
    val reversedScaleCoefficient = 1.0 / scaleCoefficient
    val centerPoint = Point(screenWidth, screenHeight) / 2.0
    val worldOriginPoint = Point(centerPoint.x - cameraPoint.x, cameraPoint.y + centerPoint.y)
    val rescaledPoint = scalePoint(point, centerPoint, reversedScaleCoefficient)
    println(rescaledPoint)
    return convertPointToAnotherSystemWithoutScaling(rescaledPoint, worldOriginPoint)
}

fun convertPointFromWorldToScreenSystem(
    point: Point,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
) : Point {
    val centerPoint = Point(screenWidth, screenHeight) / 2.0
    val screenOriginPoint = Point(cameraPoint.x - centerPoint.x, cameraPoint.y + centerPoint.y)
    val pointInScreenSystemWithoutScaling = convertPointToAnotherSystemWithoutScaling(point, screenOriginPoint)
    return scalePoint(pointInScreenSystemWithoutScaling, centerPoint, scaleCoefficient)
}

fun convertLineFromScreenToWorldSystem(
    line: Line,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Line = Line(line.points.map {
    convertPointFromScreenToWorldSystem(it, cameraPoint, screenWidth, screenHeight, scaleCoefficient)
})

fun convertLineFromWorldToScreenSystem(
    line: Line,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
) : Line = Line(line.points.map {
    convertPointFromWorldToScreenSystem(it, cameraPoint, screenWidth, screenHeight, scaleCoefficient)
})
