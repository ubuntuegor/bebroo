package to.bnt.bebroo.web.components

import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onFocusFunction
import kotlinx.html.js.onInputFunction
import kotlinx.html.style
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
import kotlin.math.round

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
                props.isReadOnly?.let { readonly = it }
                props.maxCharacters?.let { maxLength = it.toString() }
                props.isRequired?.let { required = it }
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
                props.isReadOnly?.let { readonly = it }
                props.maxCharacters?.let { maxLength = it.toString() }
                props.isRequired?.let { required = it }
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
    var isDisabled: Boolean?
}

val customCheckBox = fc<CheckBoxProps> { props ->
    styledLabel {
        css {
            +Styles.customCheckBox
        }

        input(InputType.checkBox) {
            props.isChecked?.let { attrs.checked = it }
            props.onChange?.let {
                attrs.onChangeFunction = { event -> it((event.target as HTMLInputElement).checked) }
            }
            props.isDisabled?.let { attrs.disabled = it }
        }

        div("checkbox-bar") {}
        div("checkbox-head") {}
    }
}

external interface SliderProps : Props {
    var max: Int?
    var min: Int?
    var step: Int?
    var value: Int?
    var onChange: ((Int) -> Unit)?
}

val customSlider = fc<SliderProps> { props ->
    styledDiv {
        css {
            width = 100.pct
            height = 12.px
            position = Position.relative
            child(".custom-slider-track") {
                position = Position.absolute
                width = 100.pct
                height = 4.px
                top = 4.px
                left = 0.px
                backgroundColor = Color("#e5e5e5")
                borderRadius = 4.px
            }
            child(".custom-slider-filled-track") {
                position = Position.absolute
                height = 4.px
                top = 4.px
                left = 0.px
                backgroundColor = Color("#3c3c3c")
                borderRadius = 4.px
            }
            child("input") {
                position = Position.absolute
                top = 0.px
                left = 0.px
                width = 100.pct
                height = 12.px
                margin(0.px)
                backgroundColor = Color.transparent
                outline = Outline.none
                appearance = Appearance.none
                "&::-webkit-slider-thumb" {
                    appearance = Appearance.none
                    width = 12.px
                    height = 12.px
                    backgroundColor = Color("#3c3c3c")
                    borderRadius = 50.pct
                    border = "none"
                }
                "&::-moz-range-thumb " {
                    width = 12.px
                    height = 12.px
                    backgroundColor = Color("#3c3c3c")
                    borderRadius = 50.pct
                    border = "none"
                }
            }
        }
        div("custom-slider-track") {}
        div("custom-slider-filled-track") {
            // hack to dynamically change width without temporary classes
            val filledWidth = round((props.value!! - props.min!!).toDouble() / (props.max!! - props.min!!) * 100).toString() + "%"
            attrs.style = js("{ width: filledWidth }")
        }
        input(InputType.range) {
            attrs.min = props.min!!.toString()
            attrs.max = props.max!!.toString()
            attrs.step = (props.step ?: 1).toString()
            attrs.value = props.value!!.toString()
            attrs.onInputFunction = { event -> props.onChange?.let { it((event.target as HTMLInputElement).value.toInt()) } }
        }
    }
}
