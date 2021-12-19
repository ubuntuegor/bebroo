package to.bnt.bebroo.web

import kotlinx.browser.document
import react.*
import react.dom.*
import react.router.dom.BrowserRouter
import react.router.dom.Redirect
import react.router.dom.Route
import react.router.dom.Switch
import styled.injectGlobal
import to.bnt.bebroo.web.routes.*

val app = fc<Props> {
    BrowserRouter {
        Switch {
            Route {
                attrs.path = arrayOf("/")
                attrs.exact = true
                Redirect {
                    attrs.to = "/auth"
                }
            }
            Route {
                attrs.path = arrayOf("/home")
                child(homePage)
            }
            Route {
                attrs.path = arrayOf("/auth")
                child(authPage)
            }
            Route {
                attrs.path = arrayOf("/board/:uuid")
                child(boardPage)
            }
            Route {
                attrs.path = arrayOf("*")
                child(notFoundPage)
            }
        }
    }
}

fun main() {
    injectGlobal(globalStyles.toString())
    render(document.getElementById("root")) {
        child(app)
    }
}
