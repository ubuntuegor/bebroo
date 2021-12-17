package to.bnt.bebroo.web.routes

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.br
import react.dom.h1
import react.router.dom.Link
import react.router.dom.useHistory
import styled.*
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.*
import to.bnt.draw.shared.apiClient.ApiClient
import to.bnt.draw.shared.apiClient.exceptions.ApiException
import to.bnt.draw.shared.apiClient.exceptions.InvalidTokenException
import to.bnt.draw.shared.structures.Board
import to.bnt.draw.shared.structures.User
import to.bnt.draw.shared.util.formatTimestamp

val homePage = fc<Props> {
    val client by useState(ApiClient(Config.API_PATH))
    val history = useHistory()
    var user: User? by useState(null)
    var boards: List<Board>? by useState(null)
    var showingCreateBoardModal by useState(false)
    var showingModifyUserModal by useState(false)

    val redirectToAuth = {
        history.push("/auth")
    }

    val logout = {
        window.localStorage.removeItem(Config.LOCAL_STORAGE_TOKEN_KEY)
        redirectToAuth()
    }

    useEffectOnce {
        document.title = Config.APP_NAME

        val token = window.localStorage.getItem(Config.LOCAL_STORAGE_TOKEN_KEY)
        token?.let {
            client.token = token
            MainScope().launch {
                try {
                    user = client.getMe()
                    boards = client.listBoards()
                } catch (e: InvalidTokenException) {
                    window.localStorage.removeItem(Config.LOCAL_STORAGE_TOKEN_KEY)
                    redirectToAuth()
                } catch (e: ApiException) {
                    window.alert(e.message ?: "Ошибка сервера")
                }
            }
        } ?: redirectToAuth()

        cleanup {
            client.close()
        }
    }

    pageHeader {
        styledDiv {
            css {
                position = Position.absolute
                top = 50.pct
                left = 10.px
                width = 35.pct
                transform { translateY((-50).pct) }
            }
            createButton { showingCreateBoardModal = true }
        }
        styledDiv {
            css {
                position = Position.absolute
                top = 50.pct
                right = 10.px
                width = 35.pct
                transform { translateY((-50).pct) }
            }
            user?.let {
                userManage {
                    attrs.user = it
                    attrs.onModifyUserClicked = { showingModifyUserModal = true }
                    attrs.onLogoutClicked = { logout() }
                }
            }
        }
    }

    styledMain {
        css {
            +Styles.container
            marginTop = 50.px
            marginBottom = 100.px
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = Align.center
            gap = 20.px
        }

        boards?.let {
            if (it.isEmpty()) {
                h1 {
                    +"Досок пока нет"
                }
                styledP {
                    css {
                        color = Color(Styles.neutralTextColor)
                    }
                    +"Создайте доску, чтобы начать рисовать."
                }
            } else {
                for (board in it) {
                    child(boardListElement) {
                        attrs.board = board
                    }
                }
            }
        } ?: spinner(Color.black, 50.px)

        styledP {
            css {
                marginTop = 30.px
                textAlign = TextAlign.center
                fontSize = 13.px
                color = Color(Styles.neutralTextColor)
            }
            +"Bebroo Team | SPbU"
            br {}
            styledSpan {
                css {
                    fontWeight = FontWeight.bold
                }
                +"2021"
            }
        }
    }

    // modal
    if (showingCreateBoardModal) {
        child(modal) {
            createBoardForm {
                attrs.client = client
                attrs.onClose = { showingCreateBoardModal = false }
            }
        }
    }
    if (showingModifyUserModal) {
        child(modal) {
            modifyUserForm {
                attrs.client = client
                attrs.onClose = { showingModifyUserModal = false }
            }
        }
    }
}

fun RBuilder.createButton(onClick: (Event) -> Unit) {
    styledDiv {
        attrs.onClickFunction = onClick
        css {
            display = Display.flex
            alignItems = Align.center
            fontSize = 13.px
            fontWeight = FontWeight.w500
            cursor = Cursor.pointer
        }

        styledImg("Plus icon", "/assets/images/plus_icon.svg") {
            css {
                marginRight = 16.px
            }
            attrs.width = "28px"
            attrs.height = "28px"
        }

        +"Создать доску"
    }
}

external interface UserManageProps : Props {
    var user: User
    var onModifyUserClicked: (Event) -> Unit
    var onLogoutClicked: (Event) -> Unit
}

val userManage = fc<UserManageProps> { props ->
    styledDiv {
        css {
            display = Display.flex
            alignItems = Align.center
        }

        styledDiv {
            css {
                textAlign = TextAlign.right
                marginRight = 12.px
                overflow = Overflow.hidden
            }

            styledDiv {
                css {
                    marginBottom = 2.px
                    fontSize = 14.px
                    fontWeight = FontWeight.w500
                    textOverflow = TextOverflow.ellipsis
                    overflow = Overflow.hidden
                }

                +props.user.displayName
            }

            styledDiv {
                css {
                    fontSize = 11.px
                    color = Color(Styles.neutralTextColor)
                }

                textButtonSmall {
                    attrs.onClick = props.onModifyUserClicked
                    +"Сменить имя"
                }
                +" | "
                textButtonSmall {
                    attrs.onClick = props.onLogoutClicked
                    +"Выйти"
                }
            }
        }
        profilePicture {
            attrs.user = props.user
        }
    }
}

external interface BoardListElementProps : Props {
    var board: Board
}

val boardListElement = fc<BoardListElementProps> { props ->
    Link {
        attrs.className = "${Styles.name}-${Styles::fullWidth.name}"
        attrs.to = "/board/${props.board.uuid}"

        styledDiv {
            css {
                boxSizing = BoxSizing.borderBox
                width = 100.pct
                display = Display.flex
                alignItems = Align.center
                justifyContent = JustifyContent.spaceBetween
                padding(20.px, 40.px)
                borderRadius = 100.px
                backgroundColor = Color.white
                boxShadow(Color("rgba(0,0,0,0.4)"), 0.px, 1.px, 3.px)
                transition("background-color", 0.2.s)

                active {
                    backgroundColor = Color("#ebebeb")
                }
            }

            styledDiv {
                css {
                    overflow = Overflow.hidden
                }

                styledH1 {
                    css {
                        marginBottom = 5.px
                        fontFamily = Styles.brandFontFamily
                        fontWeight = FontWeight.normal
                        fontSize = 26.px
                        overflow = Overflow.hidden
                        textOverflow = TextOverflow.ellipsis
                    }

                    +props.board.name
                }
                styledDiv {
                    css {
                        display = Display.flex
                        alignItems = Align.center
                    }

                    styledDiv {
                        css {
                            marginRight = 8.px
                            flex(.0, .0, FlexBasis.auto)
                            width = 21.px
                            height = 21.px
                            borderRadius = 50.pct
                            backgroundColor = Color("#969696")
                            backgroundSize = "cover"
                            backgroundPosition = "center"
                            props.board.creator.avatarUrl?.let { backgroundImage = Image("url(\"$it\")") }
                        }
                    }

                    styledSpan {
                        css {
                            fontSize = 14.px
                            fontWeight = FontWeight.w500
                            color = Color(Styles.neutralTextColor)
                            overflow = Overflow.hidden
                            textOverflow = TextOverflow.ellipsis
                        }
                        +props.board.creator.displayName
                    }
                }
            }

            props.board.timestamp?.let {
                styledSpan {
                    css {
                        fontSize = 14.px
                        color = Color(Styles.neutralTextColor)
                    }

                    +formatTimestamp(it)
                }
            }
        }
    }
}
