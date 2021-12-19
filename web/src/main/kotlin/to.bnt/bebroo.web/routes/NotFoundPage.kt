package to.bnt.bebroo.web.routes

import kotlinx.browser.document
import kotlinx.css.*
import react.Props
import react.dom.h1
import react.fc
import react.useEffectOnce
import styled.css
import styled.styledMain
import styled.styledP
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles
import to.bnt.bebroo.web.components.pageHeader
import to.bnt.bebroo.web.components.roundedLink

val notFoundPage = fc<Props> {
    child(pageHeader)

    useEffectOnce {
        document.title = Config.APP_NAME
    }

    styledMain {
        css {
            +Styles.container
            textAlign = TextAlign.center
            marginTop = 50.px
        }

        h1 {
            +"404"
        }
        styledP {
            css {
                color = Color(Styles.neutralTextColor)
                marginTop = 20.px
                marginBottom = 30.px
            }
            +"Такой страницы не найдено."
        }
        roundedLink {
            attrs.to = "/"
            +"На главную"
        }
    }
}
