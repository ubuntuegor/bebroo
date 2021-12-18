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
    var title: String?
    var value: String?
    var isPassword: Boolean?
    var isRequired: Boolean?
    var maxCharacters: Int?
    var onChange: ((Event) -> Unit)?
}

val roundedTextField = fc<TextFieldProps> { props ->
    styledDiv {
        css {
            +Styles.roundedTextInput
        }
        input(type = if (props.isPassword == true) InputType.password else InputType.text) {
            attrs {
                props.maxCharacters?.let { maxLength = it.toString() }
                required = props.isRequired == true
                value = props.value!!
                onChangeFunction = props.onChange!!
            }
        }
        label {
            +props.title!!
        }
    }
}
