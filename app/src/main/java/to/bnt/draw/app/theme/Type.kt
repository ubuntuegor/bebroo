package to.bnt.draw.app.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import to.bnt.draw.app.R

//TODO to delete comments
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

val fonts =
    FontFamily(
        Font(R.font.bebroosans_regular),
        Font(R.font.bebroosans_extrabold, weight = FontWeight.ExtraBold),
        Font(R.font.bebroosans_bold, weight = FontWeight.Bold),
        Font(R.font.bebroosans_semibold, weight = FontWeight.SemiBold),
        Font(R.font.bebroosans_medium, weight = FontWeight.Medium),
        Font(R.font.bebroosans_light, weight = FontWeight.Light),
        Font(R.font.bebroosans_extralight, weight = FontWeight.ExtraLight),
        Font(R.font.bebroosans_thin, weight = FontWeight.Thin)
    )