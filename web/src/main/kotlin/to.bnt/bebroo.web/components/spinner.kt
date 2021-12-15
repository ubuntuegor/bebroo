package to.bnt.bebroo.web.components

import kotlinx.css.*
import kotlinx.css.properties.*
import react.RBuilder
import styled.animation
import styled.css
import styled.styledSpan

fun RBuilder.spinner(color: Color, size: LinearDimension) {
    styledSpan {
        css {
            position = Position.relative
            display = Display.inlineBlock
            width = size
            height = size
            verticalAlign = VerticalAlign.middle

            animation(duration = 1.s, timing = Timing.linear, iterationCount = IterationCount.infinite) {
                from {
                    transform {
                        rotate(0.deg)
                    }
                }
                to {
                    transform {
                        rotate(360.deg)
                    }
                }
            }

            before {
                position = Position.absolute
                left = 0.px
                top = 0.px
                width = 100.pct
                height = 100.pct
                borderRadius = 50.pct
                boxSizing = BoxSizing.borderBox
                border = "solid 3px $color"
                opacity = 0.5
            }

            after {
                position = Position.absolute
                left = 0.px
                top = 0.px
                width = 100.pct
                height = 100.pct
                borderRadius = 50.pct
                boxSizing = BoxSizing.borderBox
                borderLeft = "solid 3px $color"
                borderTop = "solid 3px $color"
                borderRight = "solid 3px $color"
                borderBottom = "solid 3px transparent"
            }
        }
    }
}
