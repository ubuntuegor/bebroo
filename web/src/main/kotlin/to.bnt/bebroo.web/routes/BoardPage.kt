package to.bnt.bebroo.web.routes

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.boxShadow
import kotlinx.html.id
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.dom.attrs
import react.dom.canvas
import react.dom.img
import react.fc
import react.router.dom.Link
import react.router.dom.useHistory
import react.router.dom.useLocation
import react.router.dom.useParams
import react.useEffectOnce
import react.useState
import styled.*
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.*
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.exceptions.ForbiddenException
import to.bnt.draw.shared.apiClient.exceptions.InvalidTokenException
import to.bnt.draw.shared.drawing.DrawingBoard
import to.bnt.draw.shared.drawing.JsCanvas
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User

enum class ShowingPanel {
    None, HelpPanel, SharePanel
}

val boardPage = fc<Props> {
    val canvasId = "drawingCanvas"
    val client: ApiClient by useState(ApiClient(Config.API_PATH))
    val params = useParams()
    val location = useLocation()
    val history = useHistory()
    var board: Board? by useState(null)
    var usersConnected: List<User>? by useState(null)
    var canvasWidth by useState(window.innerWidth)
    var canvasHeight by useState(window.innerHeight)
    var showingPanel by useState(ShowingPanel.None)

    val redirectToAuth = {
        val queryParams = URLSearchParams()
        queryParams.append("returnUrl", location.pathname)
        history.push("/auth?$queryParams")
    }

    val redirectToHome = {
        history.push("/home")
    }

    val resizeHandler = { _: Event ->
        canvasWidth = window.innerWidth
        canvasHeight = window.innerHeight
    }

    useEffectOnce {
        window.addEventListener("resize", resizeHandler)

        val token = window.localStorage.getItem(Config.LOCAL_STORAGE_TOKEN_KEY)
        val uuid = params["uuid"] ?: throw RuntimeException("Trying to open board without uuid")

        client.token = token
        MainScope().launch {
            try {
                board = client.getBoard(uuid)
                val canvas = JsCanvas(canvasId)
                val drawingBoard = DrawingBoard(canvas)
                delay(500)
                usersConnected = listOf(
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null),
                    User(1, "test", null)
                )
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
            window.removeEventListener("resize", resizeHandler)
        }
    }

    styledDiv {
        css {
            position = Position.fixed
        }
        canvas {
            attrs {
                id = canvasId
                this.width = canvasWidth.toString()
                this.height = canvasHeight.toString()
            }
        }
    }

    styledDiv {
        css {
            position = Position.fixed
            top = 20.px
            left = 20.px
        }

        child(mainCard) {
            attrs.board = board
            attrs.usersConnected = usersConnected
        }
    }

    styledDiv {
        css {
            position = Position.fixed
            top = 20.px
            right = 20.px
        }

        child(menuButtons) {
            attrs.showingPanel = showingPanel
            attrs.onPanelChanged = { panel -> showingPanel = panel }
        }
    }
}

external interface UserListingProps : Props {
    var user: User
}

val userListing = fc<UserListingProps> { props ->
    styledDiv {
        css {
            display = Display.flex
            alignItems = Align.center
        }

        profilePicture {
            attrs.user = props.user
        }

        styledSpan {
            css {
                marginLeft = 12.px
                fontSize = 14.px
                fontWeight = FontWeight.w500
                overflow = Overflow.hidden
                textOverflow = TextOverflow.ellipsis
            }

            +props.user.displayName
        }
    }
}

external interface MainCardProps : Props {
    var board: Board?
    var usersConnected: List<User>?
}

val mainCard = fc<MainCardProps> { props ->
    styledDiv {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            width = 300.px
            maxHeight = 320.px
            padding(20.px)
            borderRadius = 26.px
            boxShadow(Color("rgba(0,0,0,0.4)"), 0.px, 1.px, 3.px)
            backgroundColor = Color.white

            props.usersConnected?.let {
                paddingBottom = 0.px
            }

            children {
                flex(.0, .0, FlexBasis.auto)
            }
        }

        styledDiv {
            css {
                marginLeft = 2.px
                marginBottom = 13.px
            }
            Link {
                attrs.to = "/home"
                img("Logo Icon", "/assets/images/logo_icon.svg") {
                    attrs.height = "30px"
                }
            }
        }

        props.board?.let {
            styledH1 {
                css {
                    fontFamily = Styles.brandFontFamily
                    fontSize = 22.px
                    fontWeight = FontWeight.normal
                    overflow = Overflow.hidden
                    textOverflow = TextOverflow.ellipsis
                    overflowWrap = OverflowWrap.breakWord
                    put("display", "-webkit-box")
                    put("-webkit-line-clamp", "2")
                    put("line-clamp", "2")
                    put("-webkit-box-orient", "vertical")
                }

                +it.name
            }
        } ?: spinner(Color.black, 50.px)

        props.usersConnected?.let {
            styledP {
                css {
                    marginTop = 8.px
                    marginBottom = 12.px
                    fontSize = 12.px
                    color = Color("#969696")
                }
                +"Сейчас редактируют:"
            }

            styledDiv {
                css {
                    flex(1.0, 1.0, FlexBasis.auto)
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = 8.px
                    overflowX = Overflow.hidden
                    overflowY = Overflow.auto
                    paddingBottom = 20.px
                }

                for (user in it) {
                    child(userListing) {
                        attrs.user = user
                    }
                }
            }
        }
    }
}

external interface MenuButtonsProps : Props {
    var showingPanel: ShowingPanel
    var onPanelChanged: (ShowingPanel) -> Unit
}

val menuButtons = fc<MenuButtonsProps> { props ->
    val getColor: (Boolean) -> String = { isActive ->
        if (isActive) Styles.accentColorLight else "#777777"
    }

    styledDiv {
        css {
            display = Display.flex
            gap = 14.px
        }

        iconButton {
            val isActive = props.showingPanel == ShowingPanel.SharePanel
            attrs.onClick = {
                val newPanel =
                    if (isActive) ShowingPanel.None else ShowingPanel.SharePanel
                props.onPanelChanged(newPanel)
            }

            shareIcon(20, getColor(isActive))
        }

        iconButton {
            val isActive = props.showingPanel == ShowingPanel.HelpPanel
            attrs.onClick = {
                val newPanel =
                    if (isActive) ShowingPanel.None else ShowingPanel.HelpPanel
                props.onPanelChanged(newPanel)
            }

            helpIcon(20, getColor(isActive))
        }
    }
}
