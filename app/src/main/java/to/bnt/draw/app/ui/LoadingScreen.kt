package to.bnt.draw.app.ui

import android.content.pm.ActivityInfo
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import to.bnt.draw.app.R
import to.bnt.draw.app.controller.BebrooController
import to.bnt.draw.app.controller.UserPreferencesManager
import to.bnt.draw.app.data.SettingsStore

@Composable
fun LoadingScreen(navController: NavController) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val settingsStoreState = UserPreferencesManager(LocalContext.current).getPreferencesFromDataStore()
        .collectAsState(initial = SettingsStore())
    BebrooController.client.token = settingsStoreState.value.token

    val scale = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f, animationSpec = tween(durationMillis = 800, easing = {
                OvershootInterpolator(4f).getInterpolation(it)
            })
        )
        delay(900L)
        if (settingsStoreState.value.token != null) {
            navController.navigate("menu") { popUpTo("loading_screen") { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo("loading_screen") { inclusive = true } }
        }
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}
