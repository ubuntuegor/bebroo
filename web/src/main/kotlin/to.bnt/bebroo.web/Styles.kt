package to.bnt.bebroo.web

import kotlinx.css.*
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import styled.StyleSheet

object Styles : StyleSheet("styles", isStatic = true) {
    const val defaultFontFamily = "Inter, Roboto, system-ui, sans-serif"
    const val accentColor = "#FF3E54"
    const val accentColorLight = "#FF7181"
    const val accentColorDark = "#E33552"
    const val accentColorHover = "#DE2B3F"

    val container by css {
        minWidth = 980.px
        maxWidth = 1200.px
        paddingLeft = 10.px
        paddingRight = 10.px
        margin = "auto"
    }

    val button by css {
        display = Display.block
        width = 100.pct
        outline = Outline.none
        fontFamily = defaultFontFamily
        fontSize = 20.px
        fontWeight = FontWeight.w600
        boxSizing = BoxSizing.borderBox
        height = 62.px
        padding(0.px, 40.px)
        border = "solid 2px #d3d3d3"
        borderRadius = 50.px
        backgroundColor = Color.white
        color = Color.black
        cursor = Cursor.pointer
        transition("background-color", 0.2.s)

        hover {
            backgroundColor = Color("#ebebeb")
        }
    }

    val buttonAccent by css {
        border = "none"
        backgroundColor = Color(accentColor)
        color = Color.white

        hover {
            backgroundColor = Color(accentColorHover)
        }
    }

    val textButton by css {
        outline = Outline.none
        fontFamily = defaultFontFamily
        fontSize = 18.px
        fontWeight = FontWeight.w600
        padding = "0px"
        border = "none"
        backgroundColor = Color.transparent
        color = Color.black
        cursor = Cursor.pointer
        transition("color", 0.2.s)

        hover {
            color = Color("#3c3c3c")
        }
    }

    val textButtonAccent by css {
        color = Color(accentColorDark)

        hover {
            color = Color(accentColorHover)
        }
    }

    val roundedTextInput by css {
        position = Position.relative
        width = 100.pct

        input {
            display = Display.block
            width = 100.pct
            height = 62.px
            padding(0.px, 26.px)
            boxSizing = BoxSizing.borderBox
            fontFamily = defaultFontFamily
            fontSize = 18.px
            backgroundColor = Color.transparent
            color = Color.black
            border = "solid 2px #d3d3d3"
            borderRadius = 50.px
            outline = Outline.none
            transition("border", 0.2.s)

            focus {
                border = "solid 2px $accentColorLight"

                adjacentSibling("label") {
                    top = (-9).px
                    color = Color(accentColorLight)
                    fontSize = 14.px
                }
            }

            valid {
                adjacentSibling("label") {
                    top = (-9).px
                    fontSize = 14.px
                }
            }
        }

        label {
            position = Position.absolute
            left = 23.px
            top = 20.px
            fontSize = 18.px
            padding(0.px, 5.px)
            backgroundColor = Color.white
            color = Color("#969696")
            transition("all", 0.2.s)
            pointerEvents = PointerEvents.none
        }
    }

    val compact by css {
        display = Display.inlineBlock
        width = LinearDimension.initial
    }
}

val globalStyles = CssBuilder(allowClasses = false).apply {
    body {
        margin(0.px)
        padding(0.px)
        fontFamily = Styles.defaultFontFamily
        backgroundColor = Color.white
        color = Color.black
    }

    fontFace {
        fontFamily = "Bebroo Sans"
        fontWeight = FontWeight.bold
        fontStyle = FontStyle.normal
        put(
            "src",
            "url(\"/fonts/BebrooSans-Bold.woff2\") format(\"woff2\")," +
                    "url(\"/fonts/BebrooSans-Bold.otf\") format(\"otf\")"
        )
    }

    fontFace {
        fontFamily = "Bebroo Sans"
        fontWeight = FontWeight.w600
        fontStyle = FontStyle.normal
        put(
            "src",
            "url(\"/fonts/BebrooSans-SemiBold.woff2\") format(\"woff2\")," +
                    "url(\"/fonts/BebrooSans-SemiBold.otf\") format(\"otf\")"
        )
    }
}
