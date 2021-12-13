package to.bnt.bebroo.web.routes

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.Draggable
import kotlinx.html.draggable
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.dom.img
import react.dom.span
import react.fc
import react.router.dom.Redirect
import react.router.dom.useHistory
import react.router.dom.useLocation
import react.useEffectOnce
import react.useState
import styled.*
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.TextFieldProps
import to.bnt.bebroo.web.components.roundedButton
import to.bnt.bebroo.web.components.roundedTextField
import to.bnt.bebroo.web.components.textButton
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.googleOAuthPopup

val authPage = fc<Props> {
    val location = useLocation()
    val token = window.localStorage.getItem("token")

    token?.let {
        val params = URLSearchParams(location.search)
        val to = params.get("returnUrl") ?: "/home"
        Redirect {
            attrs.to = to
        }
        return@fc
    }

    useEffectOnce {
        document.title = "Вход - ${Config.APP_NAME}"
    }

    styledHeader {
        css {
            +Styles.container
            display = Display.flex
            height = 100.px
            alignItems = Align.center
            justifyContent = JustifyContent.center
        }

        img("${Config.APP_NAME} Logo", "/images/logo.svg") {
            attrs.height = "37px"
            attrs.draggable = Draggable.htmlFalse
        }
    }

    styledMain {
        css {
            +Styles.container
            marginTop = 50.px
            marginBottom = 100.px
            display = Display.flex
        }
        banner()
        authenticationForm()
    }
}

val authenticationForm = fc<Props> {
    val client = ApiClient(Config.API_PATH)
    val location = useLocation()
    val history = useHistory()

    var isSignup by useState(false)
    var username by useState("")
    var password by useState("")
    var displayName by useState("")
    var isLoading by useState(false)
    var isGoogleLoading by useState(false)
    var authError: String? by useState(null)

    fun TextFieldProps.defaultFormParams() {
        required = true
        maxCharacters = 100
    }

    val handleSubmit: (Event) -> Unit = { event ->
        isLoading = true
        MainScope().launch {
            try {
                val token = if (isSignup) {
                    client.signup(username, password, displayName)
                } else {
                    client.login(username, password)
                }

                window.localStorage.setItem("token", token)
                val params = URLSearchParams(location.search)
                val to = params.get("returnUrl") ?: "/home"
                history.push(to)
            } catch (e: ApiException) {
                authError = e.message
            } finally {
                isLoading = false
            }
        }
        event.preventDefault()
    }

    val handleGoogleOAuth: (Event) -> Unit = { event ->
        isGoogleLoading = true
        MainScope().launch {
            try {
                val token = client.googleOAuthPopup().await()
                window.localStorage.setItem("token", token)
                val params = URLSearchParams(location.search)
                val to = params.get("returnUrl") ?: "/home"
                history.push(to)
            } catch (e: ApiException) {
                authError = e.message
            } finally {
                isGoogleLoading = false
            }
        }
        event.preventDefault()
    }

    styledDiv {
        css {
            width = 42.pct
            marginTop = 100.px
        }

        styledForm {
            attrs.onSubmitFunction = handleSubmit

            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.center
                gap = 18.px
            }

            if (isSignup)
                roundedTextField {
                    attrs {
                        defaultFormParams()
                        key = "displayName"
                        title = "Отображаемое имя"
                        value = displayName
                        onChange = { event -> displayName = (event.target as HTMLInputElement).value }
                    }
                }

            roundedTextField {
                attrs {
                    defaultFormParams()
                    key = "username"
                    title = "Логин"
                    value = username
                    onChange = { event -> username = (event.target as HTMLInputElement).value }
                }
            }

            roundedTextField {
                attrs {
                    defaultFormParams()
                    key = "password"
                    title = "Пароль"
                    value = password
                    this.password = true
                    onChange = { event -> password = (event.target as HTMLInputElement).value }
                }
            }

            roundedButton {
                attrs {
                    accent = true
                    loading = isLoading
                    disabled = isGoogleLoading
                }

                if (isSignup) +"Зарегистрироваться"
                else +"Войти"
            }

            textButton {
                attrs {
                    accent = true
                    onClick = {
                        isSignup = !isSignup
                        authError = null
                    }
                }

                if (isSignup) +"Вход"
                else +"Регистрация"
            }
        }

        authError?.let {
            styledP {
                css {
                    textAlign = TextAlign.center
                    color = Color.red
                    fontSize = 13.px
                    marginTop = 28.px
                    marginBottom = (-10).px
                }

                +it
            }
        }

        styledDiv {
            css {
                height = 2.px
                backgroundColor = Color("#E5E5E5")
                margin(38.px, 10.px)
            }
        }

        roundedButton {
            attrs {
                loading = isGoogleLoading
                disabled = isLoading
                onClick = handleGoogleOAuth
            }

            styledImg("Google", "/images/google_logo.svg") {
                css {
                    marginRight = 17.px
                    verticalAlign = VerticalAlign.middle
                }

                attrs.width = "24px"
                attrs.height = "24px"
            }

            +"Войти через Google"
        }
    }
}

val banner = fc<Props> {
    styledDiv {
        css {
            width = 58.pct
            height = 536.px
            backgroundImage = Image("url(\"/images/icon_epic.png\")")
            backgroundRepeat = BackgroundRepeat.noRepeat
            backgroundPosition = "left 63px center"
            backgroundSize = "contain"
        }

        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.flexStart
                marginTop = 100.px
                userSelect = UserSelect.none
                fontFamily = "\"Bebroo Sans\", sans-serif"
                fontWeight = FontWeight.w600
                fontSize = 64.px
                color = Color("#2d2d2d")

                kotlinx.css.span {
                    display = Display.inlineBlock
                    padding(0.px, 8.px)
                    backgroundColor = Color("rgba(240, 240, 240, 0.8)")
                    put("-webkit-backdrop-filter", "blur(10px)")
                    put("backdrop-filter", "blur(10px)")

                    firstChild {
                        paddingTop = 8.px
                    }
                    lastChild {
                        paddingBottom = 8.px
                    }
                }
            }
            span {
                +"Интерактивная"
            }
            span {
                +"доска"
            }
            span {
                +"для рисования"
            }
            styledSpan {
                css {
                    fontWeight = FontWeight.bold
                    color = Color(Styles.accentColorDark)
                }
                +"вместе"
            }
        }
    }
}
