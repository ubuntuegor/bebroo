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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.*


@Composable
fun Desk(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    AndroidView(
        factory = { MySurfaceView(context) },
        modifier = Modifier.padding(paddingValues)
    )
}

class MySurfaceView(context: Context?) : SurfaceView(context) {
    var path: Path? = null
    var surfaceHolder: SurfaceHolder = holder

    @Volatile
    var running = false
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var random: Random? = null

    init {
       // this.setBackgroundColor(Color.RED)
        surfaceHolder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10F
        paint.color = Color.GREEN
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