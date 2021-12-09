package to.bnt.draw.shared.drawing

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.math.PI

class JsCanvas(id: String) : SharedCanvas {
    val canvasElement = document.getElementById(id) as HTMLCanvasElement
    private val ctx = canvasElement.getContext("2d") as CanvasRenderingContext2D

    override val width
        get() = canvasElement.width
    override val height
        get() = canvasElement.height

    override fun bindEvents(board: DrawingBoard) {
        canvasElement.addEventListener("mousedown", { event  ->
            event as MouseEvent
            board.onMouseDown(Point(event.offsetX, event.offsetY))
        })
        canvasElement.addEventListener("mousemove", { event ->
            event as MouseEvent
            board.onMouseMove(Point(event.offsetX, event.offsetY))
        })
        canvasElement.addEventListener("mouseup", { event ->
            event as MouseEvent
            board.onMouseUp(Point(event.offsetX, event.offsetY))
        })
    }

    override fun drawCircle(center: Point, radius: Double, paint: Paint) {
        ctx.fillStyle = paint.fillColor
        ctx.strokeStyle = paint.strokeColor
        ctx.lineWidth = paint.strokeWidth
        ctx.beginPath()
        ctx.arc(center.x, center.y, radius, .0, 2 * PI, false)
        if (paint.strokeWidth > 0) {
            ctx.stroke()
        }
        ctx.fill()
    }
}
