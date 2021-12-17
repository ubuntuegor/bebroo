package to.bnt.draw.shared.drawing.line_algorithms

import to.bnt.draw.shared.drawing.drawing_structures.Line
import to.bnt.draw.shared.drawing.drawing_structures.Point

fun scalePoint(point: Point, centerPoint: Point, scaleCoefficient: Double): Point =
    point * scaleCoefficient + centerPoint * (1.0 - scaleCoefficient)

fun scaleLine(line: Line, centerPoint: Point, scaleCoefficient: Double): Line =
    Line(line.points.map { scalePoint(it, centerPoint, scaleCoefficient) })

fun translateLine(line: Line, translationVector: Point): Line =
    Line(line.points.map { it + translationVector })

fun convertPointToAnotherSystemWithoutScaling(point: Point, newOriginPoint: Point): Point {
    return (point - newOriginPoint).apply { y = -y }
}

fun findCenterPoint(screenWidth: Double, screenHeight: Double) = Point(screenWidth, screenHeight) / 2.0

fun convertPointFromScreenToWorldSystem(
    point: Point,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Point {
    val centerPoint = findCenterPoint(screenWidth, screenHeight)
    val worldOriginPoint = Point(centerPoint.x - cameraPoint.x, cameraPoint.y + centerPoint.y)
    val rescaledPointInScreenSystem = scalePoint(point, centerPoint, 1.0 / scaleCoefficient)
    return convertPointToAnotherSystemWithoutScaling(rescaledPointInScreenSystem, worldOriginPoint)
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
    val notScaledPointInScreenSystem = convertPointToAnotherSystemWithoutScaling(point, screenOriginPoint)
    return scalePoint(notScaledPointInScreenSystem, centerPoint, scaleCoefficient)
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

fun transformPoint(
    point: Point,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Point {
    val centerPoint = findCenterPoint(screenWidth, screenHeight)
    val newOriginPoint = cameraPoint - centerPoint
    val unscaledPoint = point - newOriginPoint
    return scalePoint(unscaledPoint, centerPoint, scaleCoefficient)
}

fun detransformPoint(
    point: Point,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Point {
    val centerPoint = findCenterPoint(screenWidth, screenHeight)
    val oldOriginPoint = -cameraPoint + centerPoint
    val rescaledPoint = scalePoint(point, centerPoint, 1.0 / scaleCoefficient)
    return rescaledPoint - oldOriginPoint
}

fun transformLine(
    line: Line,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Line = Line(line.points.map { transformPoint(it, cameraPoint, screenWidth, screenHeight, scaleCoefficient) })

fun detransformLine(
    line: Line,
    cameraPoint: Point,
    screenWidth: Double,
    screenHeight: Double,
    scaleCoefficient: Double
): Line = Line(line.points.map { detransformPoint(it, cameraPoint, screenWidth, screenHeight, scaleCoefficient) })