package to.bnt.bebroo.web.routes

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.properties.border
import kotlinx.css.px
import kotlinx.html.id
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.dom.attrs
import react.dom.h1
import react.fc
import react.router.dom.Link
import react.router.dom.useHistory
import react.router.dom.useLocation
import react.router.dom.useParams
import react.useEffectOnce
import react.useState
import styled.css
import styled.styledCanvas
import to.bnt.bebroo.web.Config
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.exceptions.ForbiddenException
import to.bnt.draw.shared.apiClient.exceptions.InvalidTokenException
import to.bnt.draw.shared.drawing.DrawingBoard
import to.bnt.draw.shared.drawing.JsCanvas
import to.bnt.draw.shared.structures.Board

val boardPage = fc<Props> {
    val client: ApiClient by useState(ApiClient(Config.API_PATH))
    val params = useParams()
    val location = useLocation()
    val history = useHistory()
    var board: Board? by useState(null)

    val redirectToAuth = {
        val queryParams = URLSearchParams()
        queryParams.append("returnUrl", location.pathname)
        history.push("/auth?$queryParams")
    }

    val redirectToHome = {
        history.push("/home")
    }

    useEffectOnce {
        val token = window.localStorage.getItem(Config.LOCAL_STORAGE_TOKEN_KEY)
        val uuid = params["uuid"] ?: throw RuntimeException("Trying to open board without uuid")

        client.token = token
        MainScope().launch {
            try {
                board = client.getBoard(uuid)
            } catch (e: InvalidTokenException) {
                window.localStorage.removeItem(Config.LOCAL_STORAGE_TOKEN_KEY)
                redirectToAuth()
            } catch (e: ForbiddenException) {
                window.alert("Недостаточно прав")
                redirectToHome()
            } catch (e: ApiException) {
                window.alert(e.message ?: "Ошибка сервера")
            }
        }

        cleanup {
            client.close()
        }
    }

    board?.let {
        h1 {
            +it.name
        }
    }

    child(drawingBoard)

    Link {
        attrs.to = "/"
        +"go to home"
    }
}

val drawingBoard = fc<Props> {
    val canvasId = "drawingCanvas"

    useEffectOnce {
        val canvas = JsCanvas(canvasId)
        val drawingBoard = DrawingBoard(canvas)
    }

    styledCanvas {
        css {
            border(1.px, BorderStyle.solid, Color.black)
        }
        attrs {
            id = canvasId
            width = "800"
            height = "600"
        }
    }
}
