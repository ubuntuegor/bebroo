package to.bnt.draw.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import to.bnt.draw.app.theme.BebrooAppTheme

//TODO Check values/colors.xml for deleting redundant colors

@Composable
fun BebrooApp() {
    BebrooAppTheme {
        val systemUiController = rememberSystemUiController()
        if (!isSystemInDarkTheme()) {
            SideEffect { systemUiController.setSystemBarsColor(color = Color.White, darkIcons = true) }
        }
        Column {
            DrawingBoardScreen("Подготовка к экзаафафафафафафафафафафамену")
            //Desk()
            //MenuScreen()
            //LoginScreen()
        }
    }
}