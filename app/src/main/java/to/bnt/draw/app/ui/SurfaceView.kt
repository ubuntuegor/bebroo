package to.bnt.draw.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.*


@Composable
fun Desk() {
    val context = LocalContext.current
    AndroidView({ MySurfaceView(context)})
}

class MySurfaceView(context: Context?) : SurfaceView(context) {
    var path: Path? = null
    var surfaceHolder: SurfaceHolder

    @Volatile
    var running = false
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var random: Random? = null

    init {
        surfaceHolder = holder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3F
        paint.color = Color.WHITE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            path = Path()
            path!!.moveTo(event.x, event.y)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            path?.lineTo(event.x, event.y)
        } else if (event.action == MotionEvent.ACTION_UP) {
            path?.lineTo(event.x, event.y)
        }
        if (path != null) {
            val canvas: Canvas = surfaceHolder.lockCanvas()
            canvas.drawPath(path!!, paint)
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
        return true
    }
}