package to.bnt.bebroo.web.components

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.Props
import react.dom.attrs
import react.dom.div
import react.dom.input
import react.dom.label
import react.fc
import styled.css
import styled.styledDiv
import styled.styledLabel
import to.bnt.bebroo.web.Styles

external interface TextFieldProps : Props {
    var title: String?
    var value: String?
    var isReadOnly: Boolean?
    var selectOnClick: Boolean?
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
                readonly = props.isReadOnly == true
                props.maxCharacters?.let { maxLength = it.toString() }
                required = props.isRequired == true
                value = props.value!!
                props.onChange?.let { onChangeFunction = it }
                if (props.selectOnClick == true) {
                    onFocusFunction = { event -> (event.target as HTMLInputElement).select() }
                }
            }
        }
        label {
            +props.title!!
        }
    }
}

val minimalTextField = fc<TextFieldProps> { props ->
    styledDiv {
        css {
            +Styles.minimalTextInput
        }
        label {
            +props.title!!
        }
        input(type = if (props.isPassword == true) InputType.password else InputType.text) {
            attrs {
                readonly = props.isReadOnly == true
                props.maxCharacters?.let { maxLength = it.toString() }
                required = props.isRequired == true
                value = props.value!!
                props.onChange?.let { onChangeFunction = it }
                if (props.selectOnClick == true) {
                    onFocusFunction = { event -> (event.target as HTMLInputElement).select() }
                }
            }
        }
    }
}

external interface CheckBoxProps : Props {
    var isChecked: Boolean?
    var onChange: ((Boolean) -> Unit)?
    var disabled: Boolean?
}

val customCheckBox = fc<CheckBoxProps> { props ->
    styledLabel {
        css {
            +Styles.customCheckBox
        }

        input(InputType.checkBox) {
            attrs.checked = props.isChecked == true
            props.onChange?.let {
                attrs.onChangeFunction = { event -> it((event.target as HTMLInputElement).checked) }
            }
            attrs.disabled = props.disabled == true
        }

        div("checkbox-bar") {}
        div("checkbox-head") {}
    }
}
