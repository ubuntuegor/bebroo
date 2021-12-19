package to.bnt.bebroo.web.components

import react.RBuilder
import react.dom.svg.ReactSVG.ellipse
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.polygon
import react.dom.svg.ReactSVG.svg

fun RBuilder.shareIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z"
        }
    }
}

fun RBuilder.helpIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"
        }
    }
}

fun RBuilder.mouseIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M13 1.07V9h7c0-4.08-3.05-7.44-7-7.93zM4 15c0 4.42 3.58 8 8 8s8-3.58 8-8v-4H4v4zm7-13.93C7.05 1.56 4 4.92 4 9h7V1.07z"
        }
    }
}

fun RBuilder.scrollIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        polygon {
            attrs.points = "13,6.99 16,6.99 12,3 8,6.99 11,6.99 11,17.01 8,17.01 12,21 16,17.01 13,17.01"
        }
    }
}

fun RBuilder.dragIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M15.54 5.54L13.77 7.3 12 5.54 10.23 7.3 8.46 5.54 12 2zm2.92 10l-1.76-1.77L18.46 12l-1.76-1.77 1.76-1.77L22 12zm-10 2.92l1.77-1.76L12 18.46l1.77-1.76 1.77 1.76L12 22zm-2.92-10l1.76 1.77L5.54 12l1.76 1.77-1.76 1.77L2 12z"
        }
        ellipse {
            attrs.cx = 12.0
            attrs.cy = 12.0
            attrs.rx = 3.0
            attrs.ry = 3.0
        }
    }
}

fun RBuilder.editIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M3 17.46v3.04c0 .28.22.5.5.5h3.04c.13 0 .26-.05.35-.15L17.81 9.94l-3.75-3.75L3.15 17.1c-.1.1-.15.22-.15.36zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
        }
    }
}

fun RBuilder.eraserIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M16.24,3.56L21.19,8.5C21.97,9.29 21.97,10.55 21.19,11.34L12,20.53C10.44,22.09 7.91,22.09 6.34,20.53L2.81,17C2.03,16.21 2.03,14.95 2.81,14.16L13.41,3.56C14.2,2.78 15.46,2.78 16.24,3.56M4.22,15.58L7.76,19.11C8.54,19.9 9.8,19.9 10.59,19.11L14.12,15.58L9.17,10.63L4.22,15.58Z"
        }
    }
}

fun RBuilder.resetIcon(size: Int, fillColor: String) {
    svg {
        attrs.width = size.toDouble()
        attrs.height = size.toDouble()
        attrs.viewBox = "0 0 24 24"
        attrs.fill = fillColor
        path {
            attrs.d = "M12,5V2L8,6l4,4V7c3.31,0,6,2.69,6,6c0,2.97-2.17,5.43-5,5.91v2.02c3.95-0.49,7-3.85,7-7.93C20,8.58,16.42,5,12,5z"
        }
        path {
            attrs.d = "M6,13c0-1.65,0.67-3.15,1.76-4.24L6.34,7.34C4.9,8.79,4,10.79,4,13c0,4.08,3.05,7.44,7,7.93v-2.02 C8.17,18.43,6,15.97,6,13z"
        }
    }
}
