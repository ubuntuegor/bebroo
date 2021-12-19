package to.bnt.bebroo.web

import kotlinx.css.*
import kotlinx.css.properties.*
import styled.StyleSheet
import styled.animation

object Styles : StyleSheet("styles", isStatic = true) {
    const val defaultFontFamily = "Inter, Roboto, system-ui, sans-serif"
    const val brandFontFamily = "\"Bebroo Sans\", $defaultFontFamily"
    const val accentColor = "#FF3E54"
    const val accentColorLight = "#FF7181"
    const val accentColorDark = "#E33552"
    const val accentColorHover = "#DE2B3F"
    const val neutralTextColor = "#777777"

    val container by css {
        boxSizing = BoxSizing.borderBox
        minWidth = 980.px
        maxWidth = 1200.px
        paddingLeft = 10.px
        paddingRight = 10.px
        margin = "auto"
    }

    val boardControl by css {
        boxSizing = BoxSizing.borderBox
        backgroundColor = Color.white
        borderRadius = 26.px
        boxShadow(Color("rgba(0,0,0,0.3)"), 0.px, 1.px, 2.px)
    }

    val boardColor by css {
        width = 30.px
        height = 30.px
        borderRadius = 50.pct
        boxSizing = BoxSizing.borderBox
        boxShadow(Color("#d3d3d3"), 0.px, 0.px, 0.px, 1.px)
        transition("border", 0.2.s)
        display = Display.flex
        alignItems = Align.center
        justifyContent = JustifyContent.center
        cursor = Cursor.pointer
    }

    val card by css {
        boxSizing = BoxSizing.borderBox
        backgroundColor = Color.white
        padding(20.px)
        borderRadius = 26.px
        boxShadow(Color("rgba(0,0,0,0.4)"), 0.px, 1.px, 3.px)
    }

    val button by css {
        display = Display.flex
        width = 100.pct
        alignItems = Align.center
        justifyContent = JustifyContent.center
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
        textDecoration = TextDecoration.none
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
        transition("color", 0.1.s)

        hover {
            color = Color("#555555")
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

    val minimalTextInput by css {
        position = Position.relative
        width = 100.pct

        input {
            display = Display.block
            width = 100.pct
            padding(4.px, 0.px)
            fontFamily = defaultFontFamily
            fontSize = 13.px
            backgroundColor = Color.transparent
            color = Color("#3c3c3c")
            border = "none"
            borderBottom = "solid 1px #d3d3d3"
            outline = Outline.none
            transition("border", 0.1.s)

            focus {
                borderBottom = "solid 1px $accentColorLight"
            }
        }

        label {
            fontSize = 11.px
            marginBottom = 2.px
            color = Color("#969696")
        }
    }

    val customCheckBox by css {
        position = Position.relative
        width = 34.px
        height = 20.px
        cursor = Cursor.pointer

        input {
            position = Position.absolute
            width = 0.px
            height = 0.px
            opacity = 0
            border = "none"

            checked {
                sibling(".checkbox-bar") {
                    backgroundColor = Color(accentColorLight)
                }
                sibling(".checkbox-head") {
                    left = 14.px
                    backgroundColor = Color(accentColor)
                }
            }
        }

        children(".checkbox-bar") {
            position = Position.absolute
            top = 3.px
            left = 0.px
            width = 100.pct
            height = 14.px
            backgroundColor = Color("#d3d3d3")
            borderRadius = 14.px
            transition("background-color", 0.2.s)
        }

        children(".checkbox-head") {
            position = Position.absolute
            top = 0.px
            left = 0.px
            width = 20.px
            height = 20.px
            backgroundColor = Color.white
            borderRadius = 50.pct
            boxShadow(Color("rgba(0,0,0,0.2)"), 0.px, 1.px, 2.px)
            transition("all", 0.2.s)
        }
    }

    val compact by css {
        display = Display.inlineFlex
        width = LinearDimension.initial
    }

    val fullWidth by css {
        display = Display.block
        width = 100.pct
    }

    val fadeIn by css {
        animation(0.2.s, Timing.ease) {
            from {
                opacity = 0
            }
            to {
                opacity = 1
            }
        }
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

    h1 {
        margin(0.px)
    }

    a {
        color = Color.inherit
        textDecoration = TextDecoration.none

        visited {
            color = Color.inherit
        }
    }

    fontFace {
        fontFamily = "Bebroo Sans"
        fontWeight = FontWeight.normal
        fontStyle = FontStyle.normal
        put(
            "src",
            "url(\"/assets/fonts/BebrooSans-Regular.woff2\") format(\"woff2\")," +
                    "url(\"/assets/fonts/BebrooSans-Regular.otf\") format(\"otf\")"
        )
    }

    fontFace {
        fontFamily = "Bebroo Sans"
        fontWeight = FontWeight.bold
        fontStyle = FontStyle.normal
        put(
            "src",
            "url(\"/assets/fonts/BebrooSans-Bold.woff2\") format(\"woff2\")," +
                    "url(\"/assets/fonts/BebrooSans-Bold.otf\") format(\"otf\")"
        )
    }

    fontFace {
        fontFamily = "Bebroo Sans"
        fontWeight = FontWeight.w600
        fontStyle = FontStyle.normal
        put(
            "src",
            "url(\"/assets/fonts/BebrooSans-SemiBold.woff2\") format(\"woff2\")," +
                    "url(\"/assets/fonts/BebrooSans-SemiBold.otf\") format(\"otf\")"
        )
    }
}
