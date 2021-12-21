package to.bnt.bebroo.web.components

import kotlinx.css.*
import kotlinx.html.Draggable
import kotlinx.html.draggable
import react.PropsWithChildren
import react.dom.img
import react.fc
import styled.css
import styled.styledHeader
import to.bnt.bebroo.web.Config
import to.bnt.bebroo.web.Styles

val pageHeader = fc<PropsWithChildren> { props ->
    styledHeader {
        css {
            +Styles.container
            position = Position.relative
            display = Display.flex
            height = 100.px
            alignItems = Align.center
            justifyContent = JustifyContent.center
        }

        img("${Config.APP_NAME} Logo", "/assets/images/logo.svg") {
            attrs.height = "37px"
            attrs.draggable = Draggable.htmlFalse
        }

        props.children()
    }
}
