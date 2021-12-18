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
    var isSubmit: Boolean
    var compact: Boolean
    var accent: Boolean
    var loading: Boolean
    var disabled: Boolean
    var onClick: (Event) -> Unit
}

val roundedButton = fc<ButtonProps> { props ->
    styledButton {
        css {
            +Styles.button
            if (props.accent) +Styles.buttonAccent
            if (props.compact) +Styles.compact
        }
        attrs.type = if (props.isSubmit) ButtonType.submit else ButtonType.button
        attrs.disabled = props.loading || props.disabled
        attrs.onClickFunction = props.onClick
        if (props.loading) {
            val spinnerColor = if (props.accent) Color.white else Color.black
            spinner(spinnerColor, 30.px)
        } else {
            props.children()
        }
    }
}

external interface TextButtonProps : PropsWithChildren {
    var accent: Boolean
    val disabled: Boolean
    var onClick: (Event) -> Unit
}

val textButton = fc<TextButtonProps> { props ->
    styledButton {
        css {
            +Styles.textButton
            if (props.accent) +Styles.textButtonAccent
        }
        attrs.disabled = props.disabled
        attrs.onClickFunction = props.onClick

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
        attrs.onClickFunction = props.onClick

        props.children()
    }
}

external interface LinkProps : PropsWithChildren {
    var to: String
    var wide: Boolean
    var accent: Boolean
}

val roundedLink = fc<LinkProps> { props ->
    Link {
        attrs.className = "${Styles.name}-${Styles::button.name}"
        if (props.accent) attrs.className += " ${Styles.name}-${Styles::buttonAccent.name}"
        if (!props.wide) attrs.className += " ${Styles.name}-${Styles::compact.name}"
        attrs.to = props.to
        props.children()
    }
}

external interface IconButtonProps : PropsWithChildren {
    var onClick: (Event) -> Unit
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

        attrs.onClickFunction = props.onClick

        props.children()
    }
}
