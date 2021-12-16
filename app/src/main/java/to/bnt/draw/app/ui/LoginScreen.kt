package to.bnt.draw.app.ui

import android.content.pm.ActivityInfo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import to.bnt.draw.app.R
import to.bnt.draw.app.controller.BebrooController
import to.bnt.draw.app.controller.UserPreferencesManager
import to.bnt.draw.app.data.SettingsStore
import to.bnt.draw.app.theme.BebrooSansFontFamily
import to.bnt.draw.app.theme.Coral
import to.bnt.draw.app.theme.SuperLightGray
import to.bnt.draw.app.theme.WhiteSemiTransparent
import to.bnt.draw.shared.apiClient.exceptions.ApiException

@Composable
fun LoginScreen(navController: NavController) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val userPreferencesStorage = UserPreferencesManager(LocalContext.current)
    Column {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.padding(top = 28.dp).fillMaxWidth().height(37.dp),
        )
        var isRegisterButtonClicked by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }
        if (isRegisterButtonClicked) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.padding(top = 40.dp).padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
                label = { Text(stringResource(R.string.nickname)) },
                singleLine = true,
                shape = CircleShape,
            )
        }
        var login by remember { mutableStateOf("") }
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            modifier = Modifier.padding(top = (14 + if (!isRegisterButtonClicked) 26 else 0).dp)
                .padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
            label = { Text(stringResource(R.string.login)) },
            singleLine = true,
            shape = CircleShape,
        )
        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.padding(top = 14.dp).padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
            label = { Text(stringResource(R.string.password)) },
            shape = CircleShape,
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        var authError by remember { mutableStateOf<String?>(null) }
        if (!isRegisterButtonClicked) {
            var isLoginButtonClicked by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    MainScope().launch {
                        try {
                            isLoginButtonClicked = true
                            val newToken = BebrooController.client.login(login, password)
                            BebrooController.client.token = newToken
                            userPreferencesStorage.saveToDataStore(SettingsStore(newToken))
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: ApiException) {
                            isLoginButtonClicked = false
                            authError = e.message
                        }
                    }
                },
                modifier = Modifier.padding(top = 14.dp).padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
                elevation = elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                shape = CircleShape,
            ) {
                if (isLoginButtonClicked) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = stringResource(R.string.enter), fontSize = 20.sp)
                }
            }
            TextButton(
                onClick = { isRegisterButtonClicked = true },
                modifier = Modifier.padding(top = 14.dp).align(Alignment.CenterHorizontally),
            ) {
                Text(text = stringResource(R.string.register), fontSize = 18.sp)
            }
        } else {
            var isSignUpButtonClicked by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    MainScope().launch {
                        try {
                            isSignUpButtonClicked = true
                            val newToken = BebrooController.client.signup(login, password, username)
                            BebrooController.client.token = newToken
                            userPreferencesStorage.saveToDataStore(SettingsStore(newToken))
                            navController.navigate("menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: ApiException) {
                            isSignUpButtonClicked = false
                            authError = e.message
                        }
                    }
                },
                modifier = Modifier.padding(top = 14.dp).padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
                elevation = elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                shape = CircleShape,
            ) {
                if (isSignUpButtonClicked) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(text = stringResource(R.string.register), fontSize = 20.sp)
                }
            }
            TextButton(
                onClick = { isRegisterButtonClicked = false },
                modifier = Modifier.padding(top = 14.dp).align(Alignment.CenterHorizontally),
            ) {
                Text(text = stringResource(R.string.enter), fontSize = 18.sp)
            }
        }
        authError?.let {
            Text(
                text = it,
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                color = Coral,
                fontSize = 16.sp
            )
        }
        Divider(color = SuperLightGray, modifier = Modifier.padding(top = 27.dp).padding(horizontal = 22.dp))
        Button(
            onClick = { print("Bebra") },
            modifier = Modifier.padding(top = 26.dp).padding(horizontal = 14.dp).fillMaxWidth().height(62.dp),
            elevation = elevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
            shape = CircleShape,
            border = BorderStroke(2.dp, Color.LightGray),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.background),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = "Google Button",
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(text = stringResource(R.string.enter_with_google), fontSize = 20.sp)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(R.drawable.cutted_icon),
                tint = Color.Unspecified,
                contentDescription = "cutted icon",
                modifier = Modifier.align(Alignment.BottomStart).padding(top = 70.dp).padding(start = 17.dp)
            )
            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 20.dp, start = 14.dp)
                    .background(WhiteSemiTransparent)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.interactive_desk_for))
                        append("\n")
                        append(stringResource(R.string.drawing))
                        append(" ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Coral)) {
                            append(stringResource(R.string.together))
                        }
                    },
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BebrooSansFontFamily
                )
            }
        }
    }
}
