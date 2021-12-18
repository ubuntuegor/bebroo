package to.bnt.bebroo.web.components

import kotlinx.css.*
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.textDecoration
import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.PropsWithChildren
import react.fc
import react.router.dom.Link
import styled.css
import styled.styledA
import styled.styledButton
import styled.styledDiv
import to.bnt.bebroo.web.Styles

external interface ButtonProps : PropsWithChildren {
    var isSubmit: Boolean?
    var compact: Boolean?
    var accent: Boolean?
    var loading: Boolean?
    var disabled: Boolean?
    var onClick: ((Event) -> Unit)?
}

val roundedButton = fc<ButtonProps> { props ->
    styledButton {
        css {
            +Styles.button
            if (props.accent == true) +Styles.buttonAccent
            if (props.compact == true) +Styles.compact
        }
        attrs.type = if (props.isSubmit == true) ButtonType.submit else ButtonType.button
        attrs.disabled = props.loading == true || props.disabled == true
        props.onClick?.let { attrs.onClickFunction = it }
        if (props.loading == true) {
            val spinnerColor = if (props.accent == true) Color.white else Color.black
            spinner(spinnerColor, 30.px)
        } else {
            props.children()
        }
    }
}

external interface TextButtonProps : PropsWithChildren {
    var accent: Boolean?
    val disabled: Boolean?
    var onClick: ((Event) -> Unit)?
}

val textButton = fc<TextButtonProps> { props ->
    styledButton {
        css {
            +Styles.textButton
            if (props.accent == true) +Styles.textButtonAccent
        }
        attrs.disabled = props.disabled == true
        props.onClick?.let { attrs.onClickFunction = it }

        props.children()
    }
}

val textButtonSmall = fc<TextButtonProps> { props ->
    styledA {
        css {
            cursor = Cursor.pointer
            hover {
                textDecoration(TextDecorationLine.underline)
            }
        }
        props.onClick?.let { attrs.onClickFunction = it }

        props.children()
    }
}

external interface LinkProps : PropsWithChildren {
    var to: String?
    var wide: Boolean?
    var accent: Boolean?
}

val roundedLink = fc<LinkProps> { props ->
    Link {
        attrs.className = "${Styles.name}-${Styles::button.name}"
        if (props.accent == true) attrs.className += " ${Styles.name}-${Styles::buttonAccent.name}"
        if (props.wide != true) attrs.className += " ${Styles.name}-${Styles::compact.name}"
        props.to?.let { attrs.to = it }
        props.children()
    }
}

external interface IconButtonProps : PropsWithChildren {
    var onClick: ((Event) -> Unit)?
}

val iconButton = fc<IconButtonProps> { props ->
    styledDiv {
        css {
            display = Display.flex
            alignItems = Align.center
            justifyContent = JustifyContent.center
            width = 40.px
            height = 40.px
            backgroundColor = Color.white
            borderRadius = 50.pct
            boxShadow(Color("rgba(0,0,0,0.25)"), 0.px, 1.px, 3.px)
            cursor = Cursor.pointer
        }

        props.onClick?.let { attrs.onClickFunction = it }

        props.children()
    }
}
