package to.bnt.draw.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import to.bnt.draw.shared.drawing.AndroidCanvas
import to.bnt.draw.shared.drawing.DrawingBoard


@Composable
fun Desk(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val canvas = AndroidCanvas(context)
    val board = DrawingBoard(canvas)

    AndroidView(
        factory = { canvas.surfaceView },
        modifier = Modifier.padding(paddingValues)
    )
}
