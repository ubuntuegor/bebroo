package to.bnt.bebroo.web.routes

import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.properties.border
import kotlinx.css.px
import kotlinx.html.id
import react.Props
import react.dom.attrs
import react.dom.h1
import react.fc
import react.router.dom.Link
import react.useEffectOnce
import styled.css
import styled.styledCanvas
import to.bnt.draw.shared.drawing.DrawingBoard
import to.bnt.draw.shared.drawing.JsCanvas

val boardPage = fc<Props> {
    val canvasId = "drawingCanvas"
    useEffectOnce {
        val canvas = JsCanvas(canvasId)
        val board = DrawingBoard(canvas)
    }

    h1 {
        +"canvasPage"
    }
    styledCanvas {
        css {
            border(1.px, BorderStyle.solid, Color.black)
        }
        attrs {
            id = canvasId
            width = "800"
            height = "600"
        }
    }
    Link {
        attrs.to = "/"
        +"go to home"
    }
}
