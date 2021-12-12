package to.bnt.draw.shared.drawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class MySurfaceView(context: Context?) : SurfaceView(context) {
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
    val surfacePaint: android.graphics.Paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

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

    override fun drawCircle(center: Point, radius: Double, paint: Paint) {
        val canvas: Canvas = surfaceView.surfaceHolder.lockCanvas()
        surfacePaint.color = Color.parseColor(paint.fillColor)
        surfacePaint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radius.toFloat(), surfacePaint)
        if (paint.strokeWidth > 0) {
            surfacePaint.color = Color.parseColor(paint.strokeColor)
            surfacePaint.strokeWidth = paint.strokeWidth.toFloat()
            surfacePaint.style = android.graphics.Paint.Style.STROKE
            canvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radius.toFloat(), surfacePaint)
        }
        surfaceView.surfaceHolder.unlockCanvasAndPost(canvas)
    }
}
