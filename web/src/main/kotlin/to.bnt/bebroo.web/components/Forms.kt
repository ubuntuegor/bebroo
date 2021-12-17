package to.bnt.bebroo.web.components

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import react.*
import react.dom.form
import react.router.dom.useHistory
import react.router.dom.useLocation
import styled.*
import to.bnt.bebroo.web.Config
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.googleOAuthPopup

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

    useEffectOnce {
        cleanup {
            client.close()
        }
    }

    fun TextFieldProps.defaultFormParams() {
        isRequired = true
        maxCharacters = 100
    }

    val handleSubmit = { event: Event ->
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
                    this.isPassword = true
                    onChange = { event -> password = (event.target as HTMLInputElement).value }
                }
            }

            roundedButton {
                attrs {
                    isSubmit = true
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

            styledImg("Google", "/assets/images/google_logo.svg") {
                css {
                    marginRight = 17.px
                }

                attrs.width = "24px"
                attrs.height = "24px"
            }

            +"Войти через Google"
        }
    }
}

external interface FormProps : Props {
    var client: ApiClient
    var onClose: (Event) -> Unit
}

val createBoardForm = fc<FormProps> { props ->
    val history = useHistory()
    var name by useState("")
    var isLoading by useState(false)

    val handleSubmit = { event: Event ->
        isLoading = true
        MainScope().launch {
            try {
                val uuid = props.client.createBoard(name)
                history.push("/board/$uuid")
            } catch (e: ApiException) {
                window.alert(e.message ?: "Ошибка сервера")
            } finally {
                isLoading = false
            }
        }
        event.preventDefault()
    }

    styledH1 {
        css {
            fontWeight = FontWeight.normal
            fontSize = 22.px
            marginBottom = 20.px
            textAlign = TextAlign.center
        }
        +"Новая доска"
    }

    form {
        attrs.onSubmitFunction = handleSubmit

        roundedTextField {
            attrs {
                isRequired = true
                maxCharacters = 200
                key = "boardName"
                title = "Название"
                value = name
                onChange = { event -> name = (event.target as HTMLInputElement).value }
            }
        }

        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.center
                gap = 20.px
                marginTop = 15.px
                marginBottom = 15.px
            }

            roundedButton {
                attrs {
                    isSubmit = true
                    accent = true
                    loading = isLoading
                }

                +"Создать"
            }

            textButton {
                attrs {
                    onClick = props.onClose
                }

                +"Отмена"
            }
        }
    }
}

val modifyUserForm = fc<FormProps> { props ->
    var displayName by useState("")
    var isLoading by useState(false)

    val handleSubmit = { event: Event ->
        isLoading = true
        MainScope().launch {
            try {
                props.client.modifyMe(displayName)
                window.location.reload()
            } catch (e: ApiException) {
                window.alert(e.message ?: "Ошибка сервера")
            } finally {
                isLoading = false
            }
        }
        event.preventDefault()
    }

    styledH1 {
        css {
            fontWeight = FontWeight.normal
            fontSize = 22.px
            marginBottom = 20.px
            textAlign = TextAlign.center
        }
        +"Изменить имя"
    }

    form {
        attrs.onSubmitFunction = handleSubmit

        roundedTextField {
            attrs {
                isRequired = true
                maxCharacters = 100
                key = "displayName"
                title = "Новое имя"
                value = displayName
                onChange = { event -> displayName = (event.target as HTMLInputElement).value }
            }
        }

        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.center
                gap = 20.px
                marginTop = 15.px
                marginBottom = 15.px
            }

            roundedButton {
                attrs {
                    isSubmit = true
                    accent = true
                    loading = isLoading
                }

                +"Изменить"
            }

            textButton {
                attrs {
                    onClick = props.onClose
                }

                +"Отмена"
            }
        }
    }
}
