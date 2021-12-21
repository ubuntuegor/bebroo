package to.bnt.bebroo.web.components

import csstype.important
import kotlinx.css.*
import kotlinx.css.properties.Timing
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.s
import react.PropsWithChildren
import react.fc
import styled.animation
import styled.css
import styled.styledDiv
import to.bnt.bebroo.web.Styles

val modal = fc<PropsWithChildren> { props ->
    styledDiv {
        css {
            position = Position.fixed
            left = 0.px
            top = 0.px
            width = 100.pct
            height = 100.pct
            backgroundColor = Color("rgba(0,0,0,0.7)")
            animation(0.3.s, Timing.ease) {
                from {
                    opacity = 0
                }
                to {
                    opacity = 1
                }
            }
        }

        styledDiv {
            css {
                +Styles.card
                margin(150.px, LinearDimension.auto)
                width = 500.px
                boxShadow(Color("rgba(0, 0, 0, 0.4)"), 0.px, 1.px, important(15.px))
            }

            props.children()
        }
    }
}
