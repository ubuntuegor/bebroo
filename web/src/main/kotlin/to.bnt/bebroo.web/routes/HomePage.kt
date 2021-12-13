package to.bnt.bebroo.web.routes

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Color
import kotlinx.css.px
import org.w3c.dom.url.URLSearchParams
import react.*
import react.dom.html.ReactHTML.br
import react.dom.img
import react.router.dom.useHistory
import react.router.dom.useLocation
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.components.roundedButton
import to.bnt.bebroo.web.components.spinner
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.exceptions.InvalidTokenException
import to.bnt.draw.shared.structures.User

val homePage = fc<Props> {
    val client = ApiClient(Config.API_PATH)
    val history = useHistory()
    val location = useLocation()
    var hasLoaded by useState(false)
    var user: User? by useState(null)

    val redirectToAuth = {
        window.localStorage.removeItem("token")
        val params = URLSearchParams()
        params.append("returnUrl", location.pathname)
        history.push("/auth?$params")
    }

    useEffectOnce {
        val token = window.localStorage.getItem("token")
        token?.let {
            client.token = token
            MainScope().launch {
                user = try {
                    client.getMe()
                } catch (e: InvalidTokenException) {
                    redirectToAuth()
                    null
                } catch (e: ApiException) {
                    window.alert(e.message ?: "Ошибка сервера")
                    null
                }
                hasLoaded = true
            }
        } ?: redirectToAuth()
    }

    if (hasLoaded) {
        +"id: ${user?.id}"
        br {}
        +"displayName: ${user?.displayName}"
        br {}
        img("Profile picture", user?.avatarUrl) {}
        br {}
        roundedButton {
            attrs.compact = true
            attrs.onClick = {
                redirectToAuth()
            }

            +"Выйти из аккаунта"
        }
    } else {
        spinner(Color.black, 50.px)
    }
}
