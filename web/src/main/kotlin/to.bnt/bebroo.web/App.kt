package to.bnt.bebroo.web

import kotlinx.browser.document
import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.properties.border
import kotlinx.css.px
import kotlinx.html.id
import react.*
import react.dom.*
import react.router.dom.BrowserRouter
import react.router.dom.Link
import react.router.dom.Route
import react.router.dom.Switch
import styled.css
import styled.styledCanvas
import to.bnt.draw.shared.Platform
import to.bnt.draw.shared.drawing.DrawingBoard
import to.bnt.draw.shared.drawing.JsCanvas

val page1 = fc<Props> {
    h1 {
        +"page1"
    }
    Link {
        attrs.to = "/"
        +"go home"
    }
}

val canvasPage = fc<PropsWithChildren> {
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
        attrs.to = "/page1"
        +"go to page 1"
    }
}

val app = fc<Props> {
    BrowserRouter {
        Switch {
            Route {
                attrs.path = arrayOf("/")
                attrs.exact = true
                canvasPage()
            }
            Route {
                attrs.path = arrayOf("/page1")
                page1()
            }
        }
    }
}

fun main() {
    render(document.getElementById("root")) {
        child(app)
    }
}

fun greet() = "Hello, ${Platform().platform}"
