package to.bnt.draw.shared.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.PixelFormat
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import to.bnt.draw.shared.drawing.drawing_structures.Point

class MySurfaceView(context: Context?) : SurfaceView(context) {
    init {
        super.setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    var surfaceHolder: SurfaceHolder = holder

    var onTouchListener: ((MotionEvent) -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchListener?.let { it -> it(event) }
        return true
    }
}

class AndroidCanvas(context: Context?) : SharedCanvas {
    val surfaceView = MySurfaceView(context)
    private val surfacePaint: android.graphics.Paint = Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    override val width
        get() = surfaceView.width
    override val height
        get() = surfaceView.height

    override fun bindEvents(board: DrawingBoard) {
        surfaceView.onTouchListener = { event ->
            val point = Point(event.x.toDouble(), event.y.toDouble())
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    board.onMouseDown(point)
                }
                MotionEvent.ACTION_MOVE -> {
                    board.onMouseMove(point)
                }
                MotionEvent.ACTION_UP -> {
                    board.onMouseUp(point)
                }
            }
        }
    }

    override fun drawLine(points: List<Point>, paint: Paint) {
        val canvas: Canvas = surfaceView.surfaceHolder.lockCanvas()
        val path = Path()

        for (point in points) {
            if (path.isEmpty) path.moveTo(point.x.toFloat(), point.y.toFloat())
            else path.lineTo(point.x.toFloat(), point.y.toFloat())
        }

        surfacePaint.color = Color.parseColor(paint.strokeColor)
        surfacePaint.style = android.graphics.Paint.Style.STROKE
        surfacePaint.strokeWidth = paint.strokeWidth.toFloat()

        canvas.drawPath(path, surfacePaint)

        surfaceView.surfaceHolder.unlockCanvasAndPost(canvas)
    }

    override fun clear() {
        val canvas: Canvas = surfaceView.surfaceHolder.lockCanvas()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        surfaceView.surfaceHolder.unlockCanvasAndPost(canvas)
    }

    override fun cleanup() {}
}
