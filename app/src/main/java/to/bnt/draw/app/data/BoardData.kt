package to.bnt.draw.app.data

import androidx.compose.ui.graphics.Color
import to.bnt.draw.app.theme.DarkGray

data class Brush(val color: Color, val stroke: Int=10)

val  defaultBrushes = listOf(
    Brush(Color(0xFF38CA46) ),
    Brush(Color(0xFFF85353)),
    Brush(Color(0xFF388CCA)),
    Brush(Color(0xFF9238CA)),
    Brush(Color(0xFFFA78C6)),
    Brush(DarkGray),
)