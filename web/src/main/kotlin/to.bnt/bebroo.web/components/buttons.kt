package to.bnt.bebroo.web.components

import kotlinx.css.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.*
import styled.css
import styled.styledButton
import to.bnt.bebroo.web.Styles

external interface ButtonProps : PropsWithChildren {
    var compact: Boolean
    var accent: Boolean
    var loading: Boolean
    var disabled: Boolean
    var onClick: (Event) -> Unit
    var onSubmit: (Event) -> Unit
}

val roundedButton = fc<ButtonProps> { props ->
    styledButton {
        css {
            +Styles.button
            if (props.accent) +Styles.buttonAccent
            if (props.compact) +Styles.compact
        }
        attrs.disabled = props.loading || props.disabled
        attrs.onClickFunction = props.onClick
        attrs.onSubmitFunction = props.onSubmit
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
    var onSubmit: (Event) -> Unit
}

val textButton = fc<TextButtonProps> { props ->
    styledButton {
        css {
            +Styles.textButton
            if (props.accent) +Styles.textButtonAccent
        }
        attrs.disabled = props.disabled
        attrs.onClickFunction = props.onClick
        attrs.onSubmitFunction = props.onSubmit

        props.children()
    }
}
