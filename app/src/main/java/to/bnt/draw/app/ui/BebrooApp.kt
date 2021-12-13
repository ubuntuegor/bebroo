package to.bnt.draw.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import to.bnt.draw.app.model.BebrooController
import to.bnt.draw.app.theme.BebrooAppTheme

//TODO Check values/colors.xml for deleting redundant colors

@Composable
fun BebrooApp() {
    BebrooAppTheme {
        val systemUiController = rememberSystemUiController()
        if (!isSystemInDarkTheme()) {
            SideEffect { systemUiController.setSystemBarsColor(color = Color.White, darkIcons = true) }
        }
        val navController = rememberNavController()
        if (!BebrooController.isTokenExist()) {
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginScreen(navController) }
                composable("menu") { MenuScreen(navController) }
                composable(
                    route = "board/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    entry.arguments?.getString("id")?.let {
                        BoardScreen(navController, it)
                    }
                }
            }
        } else {
            NavHost(navController = navController, startDestination = "menu") {
                composable("menu") { MenuScreen(navController) }
                composable(
                    route = "board/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    entry.arguments?.getString("id")?.let {
                        BoardScreen(navController,it)
                    }
                }
            }
        }
    }
}