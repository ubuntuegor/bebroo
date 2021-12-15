package to.bnt.bebroo.web.components

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.events.Event
import react.Props
import react.dom.attrs
import react.dom.input
import react.dom.label
import react.fc
import styled.css
import styled.styledDiv
import to.bnt.bebroo.web.Styles

external interface TextFieldProps : Props {
    var title: String
    var value: String
    var password: Boolean
    var required: Boolean
    var maxCharacters: Int
    var onChange: (Event) -> Unit
}

val roundedTextField = fc<TextFieldProps> { props ->
    styledDiv {
        css {
            +Styles.roundedTextInput
        }
        input(type = if (props.password) InputType.password else InputType.text) {
            attrs {
                maxLength = props.maxCharacters.toString()
                required = props.required
                value = props.value
                onChangeFunction = props.onChange
            }
        }
        label {
            +props.title
        }
    }
}
