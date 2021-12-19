package to.bnt.draw.shared.drawing

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import to.bnt.draw.shared.drawing.drawing_structures.Point

class JsCanvas(id: String) : SharedCanvas {
    private val canvasElement = document.getElementById(id) as HTMLCanvasElement
    private val ctx = canvasElement.getContext("2d") as CanvasRenderingContext2D

    override val width
        get() = canvasElement.width
    override val height
        get() = canvasElement.height

    private var resizeHandler: ((Event) -> Unit)? = null

    override fun bindEvents(board: DrawingBoard) {
        canvasElement.addEventListener("mousedown", { event ->
            event as MouseEvent
            if (event.altKey) board.onWheelDown()
            else
                when (event.button.toInt()) {
                    0 -> board.onMouseDown(Point(event.offsetX, event.offsetY))
                    1 -> board.onWheelDown()
                }
        })
        canvasElement.addEventListener("mousemove", { event ->
            event as MouseEvent
            board.onMouseMove(Point(event.offsetX, event.offsetY))
        })
        canvasElement.addEventListener("mouseup", { event ->
            event as MouseEvent
            board.onWheelUp()
            if (event.button.toInt() == 0) {
                board.onMouseUp(Point(event.offsetX, event.offsetY))
            }
        })
        canvasElement.addEventListener("wheel", { event ->
            event as WheelEvent
            board.onMouseWheel(event.deltaY)
        })
        resizeHandler = {
            board.onResize()
        }
        window.addEventListener("resize", resizeHandler)
    }

    override fun drawLine(points: List<Point>, paint: Paint) {
        ctx.fillStyle = paint.fillColor
        ctx.strokeStyle = paint.strokeColor
        ctx.lineWidth = paint.strokeWidth
        ctx.lineCap = CanvasLineCap.Companion.ROUND
        ctx.lineJoin = CanvasLineJoin.Companion.ROUND
        ctx.beginPath()
        points.firstOrNull()?.let { ctx.moveTo(it.x, it.y) }
        for (point in points) {
            ctx.lineTo(point.x, point.y)
        }
        ctx.stroke()
        ctx.closePath()
    }

    override fun clear() {
        ctx.clearRect(
            0.0,
            0.0,
            canvasElement.width.toDouble(),
            canvasElement.height.toDouble()
        )
    }

    override fun cleanup() {
        window.removeEventListener("resize", resizeHandler)
    }
}
