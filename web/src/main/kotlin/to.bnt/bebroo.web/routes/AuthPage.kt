package to.bnt.bebroo.web.routes

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.*
import org.w3c.dom.url.URLSearchParams
import react.Props
import react.dom.span
import react.fc
import react.router.dom.Redirect
import react.router.dom.useLocation
import react.useEffectOnce
import styled.css
import styled.styledDiv
import styled.styledMain
import styled.styledSpan
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.authenticationForm
import to.bnt.bebroo.web.components.pageHeader

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

    pageHeader()

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

val banner = fc<Props> {
    styledDiv {
        css {
            width = 58.pct
            height = 536.px
            backgroundImage = Image("url(\"/assets/images/icon_epic.png\")")
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
                fontFamily = Styles.brandFontFamily
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
