package to.bnt.bebroo.web.routes

import csstype.important
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import react.*
import react.dom.*
import react.router.dom.Link
import react.router.dom.useHistory
import react.router.dom.useLocation
import react.router.dom.useParams
import styled.*
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.*
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.exceptions.ForbiddenException
import to.bnt.draw.shared.apiClient.exceptions.InvalidTokenException
import to.bnt.draw.shared.drawing.DrawingBoard
import to.bnt.draw.shared.drawing.DrawingBoardWebSocket
import to.bnt.draw.shared.drawing.JsCanvas
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import kotlin.math.round

enum class ShowingPanel {
    None, HelpPanel, SharePanel
}

val boardPage = fc<Props> {
    val canvasId = "drawingCanvas"

    val params = useParams()
    val location = useLocation()
    val history = useHistory()

    val client: ApiClient by useState(ApiClient(Config.API_PATH))
    var user: User? by useState(null)
    var board: Board? by useState(null)
    var connectedUsers: List<User> by useState(listOf())

    var drawingBoard: DrawingBoardWebSocket? by useState(null)
    var showingPanel by useState(ShowingPanel.None)

    val redirectToAuth = {
        val queryParams = URLSearchParams()
        queryParams.append("returnUrl", location.pathname)
        history.push("/auth?$queryParams")
    }

    val redirectToHome = {
        history.push("/home")
    }

    useEffect {
        board?.let {
            document.title = "${it.name} - ${Config.APP_NAME}"
        } ?: run {
            document.title = Config.APP_NAME
        }
    }

    useEffectOnce {
        val token = window.localStorage.getItem(Config.LOCAL_STORAGE_TOKEN_KEY)
        val uuid = params["uuid"] ?: throw RuntimeException("Trying to open board without uuid")

        client.token = token
        MainScope().launch {
            try {
                val loadedUser = token?.let { client.getMe() }
                user = loadedUser
                board = client.getBoard(uuid)
                val canvas = JsCanvas(canvasId)
                drawingBoard = DrawingBoardWebSocket(canvas, client, uuid, loadedUser?.id)
            } catch (e: InvalidTokenException) {
                window.localStorage.removeItem(Config.LOCAL_STORAGE_TOKEN_KEY)
                redirectToAuth()
            } catch (e: ForbiddenException) {
                window.alert("???????????????????????? ????????")
                redirectToHome()
            } catch (e: ApiException) {
                window.alert(e.message ?: "???????????? ??????????????")
            }
        }

        cleanup {
            client.close()
        }
    }

    boardCanvas {
        attrs.canvasId = canvasId
        attrs.drawingBoard = drawingBoard
        attrs.onConnectionClosed = { connectedUsers = listOf() }
        attrs.onApiException = { e: ApiException -> window.alert(e.message ?: "???????????? ?????????????????????? ?? ??????????") }
        attrs.connectedUsers = connectedUsers
        attrs.onConnectedUsersChange = { connectedUsers = it }
    }

    styledDiv {
        css {
            position = Position.fixed
            top = 20.px
            left = 20.px
        }

        child(mainCard) {
            attrs.board = board
            attrs.usersConnected = connectedUsers
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

    styledDiv {
        css {
            position = Position.fixed
            top = 80.px
            right = 20.px
        }

        when (showingPanel) {
            ShowingPanel.HelpPanel -> child(helpCard)
            ShowingPanel.SharePanel -> child(shareCard) {
                attrs.client = client
                attrs.user = user
                attrs.board = board
                attrs.onBoardChanged = { newBoard ->
                    board = newBoard
                }
            }
            else -> {}
        }
    }

    // Board controls
    drawingBoard?.let {
        user?.let {
            styledDiv {
                css {
                    position = Position.fixed
                    left = 20.px
                    bottom = 20.px
                }

                colorSelector {
                    attrs.drawingBoard = drawingBoard
                }
            }

            styledDiv {
                css {
                    position = Position.fixed
                    left = 20.px
                    bottom = 80.px
                }

                widthSelector {
                    attrs.drawingBoard = drawingBoard
                }
            }
        } ?: run {
            styledDiv {
                css {
                    position = Position.fixed
                    left = 20.px
                    bottom = 20.px
                }

                roundedLink {
                    val queryParams = URLSearchParams()
                    queryParams.append("returnUrl", location.pathname)
                    attrs.to = "/auth?$queryParams"
                    attrs.accent = true

                    +"??????????"
                }
            }
        }

        styledDiv {
            css {
                position = Position.fixed
                right = 20.px
                bottom = 20.px
            }

            zoomControl {
                attrs.drawingBoard = drawingBoard
            }
        }
    }
}

external interface BoardCanvasProps : Props {
    var canvasId: String
    var drawingBoard: DrawingBoardWebSocket?
    var onConnectionClosed: () -> Unit
    var onApiException: (ApiException) -> Unit
    var connectedUsers: List<User>
    var onConnectedUsersChange: (List<User>) -> Unit
}

val boardCanvas = fc<BoardCanvasProps> { props ->
    var canvasWidth by useState(window.innerWidth)
    var canvasHeight by useState(window.innerHeight)

    val resizeHandler = { _: Event ->
        canvasWidth = window.innerWidth
        canvasHeight = window.innerHeight
    }

    useEffectOnce {
        window.addEventListener("resize", resizeHandler)

        cleanup {
            window.removeEventListener("resize", resizeHandler)
        }
    }

    useEffect {
        props.drawingBoard?.onConnectionClosed = props.onConnectionClosed
        props.drawingBoard?.onApiException = props.onApiException
        props.drawingBoard?.onConnectedUsers = {
            props.onConnectedUsersChange(props.connectedUsers + it)
        }
        props.drawingBoard?.onDisconnectedUser = { userId ->
            val newUsers = props.connectedUsers.toMutableList()
            newUsers.lastOrNull { it.id == userId }?.let {
                newUsers -= it
            }
            props.onConnectedUsersChange(newUsers)
        }
    }

    useEffect(props.drawingBoard) {
        props.drawingBoard?.connect()

        cleanup {
            props.drawingBoard?.cleanup()
        }
    }

    styledDiv {
        css {
            position = Position.fixed
        }
        canvas {
            attrs {
                id = props.canvasId
                this.width = canvasWidth.toString()
                this.height = canvasHeight.toString()
            }
        }
    }
}

external interface UserListingProps : Props {
    var user: User?
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

            +(props.user?.displayName ?: "?????????????????????? ????????????????????????")
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
            +Styles.card
            display = Display.flex
            flexDirection = FlexDirection.column
            width = 300.px
            maxHeight = 320.px

            props.usersConnected?.let {
                paddingBottom = important(0.px)
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
            props.board?.let {
                styledP {
                    css {
                        marginTop = 8.px
                        marginBottom = 12.px
                        fontSize = 12.px
                        color = Color("#969696")
                    }
                    +"???????????? ??????????????????????:"
                }
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
    var showingPanel: ShowingPanel?
    var onPanelChanged: ((ShowingPanel) -> Unit)?
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
                props.onPanelChanged?.let { it(newPanel) }
            }

            shareIcon(20, getColor(isActive))
        }

        iconButton {
            val isActive = props.showingPanel == ShowingPanel.HelpPanel
            attrs.onClick = {
                val newPanel =
                    if (isActive) ShowingPanel.None else ShowingPanel.HelpPanel
                props.onPanelChanged?.let { it(newPanel) }
            }

            helpIcon(20, getColor(isActive))
        }
    }
}

val helpCard = fc<Props> {
    val iconSize = 24
    val iconColor = "#777777"
    styledDiv {
        css {
            +Styles.card
            +Styles.fadeIn
            display = Display.flex
            flexDirection = FlexDirection.column
            gap = 14.px
            width = 300.px
            fontSize = 13.px
            color = Color("#969696")

            children("div") {
                display = Display.flex
                gap = 12.px
                alignItems = Align.center

                children("svg") {
                    flex(.0, .0, FlexBasis.auto)
                }
            }
        }

        div {
            mouseIcon(iconSize, iconColor)
            +"?????????? ????????????????, ?????????????? ?????????? ???????????? ????????"
        }
        div {
            scrollIcon(iconSize, iconColor)
            +"?????????? ???????????????? ??????????????, ?????????????????? ??????????????????"
        }
        div {
            dragIcon(iconSize, iconColor)
            +"?????????? ???????????????? ??????????????, ?????????????????????? ?????????????? ???????????? ???????? ?????? ?????????????? Alt"
        }
    }
}

external interface ShareCardProps : Props {
    var client: ApiClient?
    var user: User?
    var board: Board?
    var onBoardChanged: ((Board) -> Unit)?
}

val shareCard = fc<ShareCardProps> { props ->
    val location = useLocation()
    var contributors: List<User>? by useState(null)
    var isLoading by useState(false)

    useEffectOnce {
        props.board?.let { board ->
            MainScope().launch {
                try {
                    contributors = props.client?.getBoard(board.uuid, true)?.contributors
                } catch (e: ApiException) {
                    console.warn("Failed to get contributors: ${e.message}")
                }
            }
        }
    }

    val changePublicHandler: (Boolean) -> Unit = { newState ->
        val oldState = props.board!!.isPublic
        isLoading = true
        props.onBoardChanged!!.invoke(props.board!!.copy(isPublic = newState))
        MainScope().launch {
            try {
                props.client!!.modifyBoard(props.board!!.uuid, isPublic = newState)
            } catch (e: ApiException) {
                window.alert(e.message ?: "???? ?????????????? ???????????????? ??????????")
                props.onBoardChanged!!.invoke(props.board!!.copy(isPublic = oldState))
            } finally {
                isLoading = false
            }
        }
    }

    props.board?.let { board ->
        styledDiv {
            css {
                +Styles.card
                +Styles.fadeIn
                display = Display.flex
                flexDirection = FlexDirection.column
                width = 300.px
                maxHeight = 390.px

                contributors?.let {
                    paddingBottom = important(0.px)
                }

                children {
                    flex(.0, .0, FlexBasis.auto)
                }
            }

            props.user?.let { user ->
                if (user.id == board.creator.id)
                    styledDiv {
                        css {
                            marginBottom = 20.px
                            display = Display.flex
                            alignItems = Align.center
                            justifyContent = JustifyContent.spaceBetween
                            fontSize = 13.px
                        }

                        +"?????????????? ??????????????????"

                        customCheckBox {
                            attrs.isChecked = board.isPublic
                            attrs.isDisabled = isLoading
                            attrs.onChange = changePublicHandler
                        }
                    }
            }

            if (board.isPublic)
                styledDiv {
                    css {
                        marginBottom = 12.px
                    }
                    minimalTextField {
                        attrs {
                            title = "????????????:"
                            isReadOnly = true
                            selectOnClick = true
                            value = window.location.origin + location.pathname
                        }
                    }
                }

            styledP {
                css {
                    marginTop = 0.px
                    marginBottom = 12.px
                    fontSize = 11.px
                    color = Color("#969696")
                }

                +"?????????????????? ??????????:"
            }

            userListing {
                attrs.user = board.creator
            }

            contributors?.let {
                styledP {
                    css {
                        marginTop = 12.px
                        marginBottom = 12.px
                        fontSize = 11.px
                        color = Color("#969696")
                    }

                    +"??????????????????:"
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
                            attrs.key = user.id.toString()
                            attrs.user = user
                        }
                    }
                }
            }
        }
    }
}

external interface DrawingBoardToolProps : Props {
    var drawingBoard: DrawingBoard?
}

private enum class SelectedTool {
    Color, CustomColor, Eraser
}

val colorSelector = fc<DrawingBoardToolProps> { props ->
    val colors = DrawingBoard.defaultColors
    var tool by useState(SelectedTool.Color)
    var selectedColor by useState(props.drawingBoard!!.strokeColor)
    var customColor by useState("")

    useEffect {
        if (tool != SelectedTool.CustomColor && customColor != "")
            customColor = ""
    }

    useEffect {
        if (tool != SelectedTool.Eraser) props.drawingBoard?.isEraser = false
        when (tool) {
            SelectedTool.Color -> {
                props.drawingBoard?.strokeColor = selectedColor
            }
            SelectedTool.CustomColor -> {
                props.drawingBoard?.strokeColor = customColor.ifEmpty { "#000000" }
            }
            SelectedTool.Eraser -> {
                props.drawingBoard?.isEraser = true
            }
        }
    }

    styledDiv {
        css {
            +Styles.boardControl
            height = 50.px
            padding(10.px)
            display = Display.flex
            gap = 16.px
        }

        for (color in colors) {
            styledDiv {
                css {
                    +Styles.boardColor
                    backgroundColor = Color(color)
                    border = "solid 8px white"
                    if (tool == SelectedTool.Color && color == selectedColor) {
                        border = "solid 4px white"
                    }
                }
                attrs.onClickFunction = { tool = SelectedTool.Color; selectedColor = color }
            }
        }

        styledLabel {
            css {
                +Styles.boardColor
                backgroundColor = Color.white
                border = "solid 8px white"
                if (tool == SelectedTool.CustomColor) {
                    backgroundColor = Color(customColor)
                    border = "solid 4px white"
                }

                children("input") {
                    position = Position.absolute
                    width = 0.px
                    height = 0.px
                    opacity = 0
                    padding(0.px)
                    border = "none"
                    pointerEvents = PointerEvents.none
                }

                children("svg") {
                    flex(.0, .0, FlexBasis.auto)
                }
            }

            input(InputType.color) {
                attrs.value = customColor
                attrs.onChangeFunction = {
                    tool = SelectedTool.CustomColor
                    customColor = (it.target as HTMLInputElement).value
                }
            }

            if (tool == SelectedTool.CustomColor) {
                editIcon(14, "#ffffff")
            } else {
                editIcon(16, "#777777")
            }
        }

        styledDiv {
            css {
                +Styles.boardColor
                backgroundColor = Color.white
            }
            attrs.onClickFunction = { tool = SelectedTool.Eraser }
            if (tool == SelectedTool.Eraser) {
                eraserIcon(16, Styles.accentColorLight)
            } else {
                eraserIcon(16, "#777777")
            }
        }
    }
}

val widthSelector = fc<DrawingBoardToolProps> { props ->
    val widthRange = DrawingBoard.strokeWidthRange
    var strokeWidth by useState(DrawingBoard.defaultStrokeWidth)

    useEffect {
        props.drawingBoard?.strokeWidth = strokeWidth
    }

    styledDiv {
        css {
            +Styles.boardControl
            width = 200.px
            padding(15.px)
        }

        styledDiv {
            css {
                display = Display.flex
                alignItems = Align.end
                justifyContent = JustifyContent.spaceBetween
                marginBottom = 6.px
            }

            styledDiv {
                css {
                    width = 8.px
                    height = 8.px
                    borderRadius = 50.pct
                    backgroundColor = Color("#3c3c3c")
                }
            }
            styledDiv {
                css {
                    width = 20.px
                    height = 20.px
                    borderRadius = 50.pct
                    backgroundColor = Color("#3c3c3c")
                }
            }
        }

        styledDiv {
            css {
                paddingLeft = 4.px
                paddingRight = 10.px
            }

            customSlider {
                attrs.min = widthRange.first
                attrs.max = widthRange.last
                attrs.step = widthRange.step
                attrs.value = strokeWidth
                attrs.onChange = { strokeWidth = it }
            }
        }
    }
}

val zoomControl = fc<DrawingBoardToolProps> { props ->
    var scale by useState(1.0)

    useEffectOnce {
        props.drawingBoard!!.onScaleChanged = {
            scale = it
        }

        cleanup {
            props.drawingBoard!!.onScaleChanged = {}
        }
    }

    val resetHandler: (Event) -> Unit = { _: Event ->
        props.drawingBoard?.setScale(1.0)
    }

    styledDiv {
        css {
            +Styles.boardControl
            height = 30.px
            padding(5.px)
            display = Display.flex
            alignItems = Align.center
        }

        styledDiv {
            css {
                padding(0.px, 12.px)
                fontSize = 14.px
            }

            val zoom = round(scale * 100)
            +"$zoom%"
        }

        styledDiv {
            css {
                height = 20.px
                cursor = Cursor.pointer
                borderRadius = 50.pct
                backgroundColor = Color.white
                transition("background-color", 0.2.s)
                active {
                    backgroundColor = Color("#ebebeb")
                }
            }
            attrs.onClickFunction = resetHandler

            resetIcon(20, "#000000")
        }
    }
}
