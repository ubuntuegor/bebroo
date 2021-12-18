package to.bnt.bebroo.web.components

import kotlinx.css.*
import react.Props
import react.fc
import styled.css
import styled.styledDiv
import to.bnt.draw.shared.structures.User

external interface ProfilePictureProps : Props {
    var user: User
}

val profilePicture = fc<ProfilePictureProps> { props ->
    styledDiv {
        css {
            width = 32.px
            height = 32.px
            borderRadius = 50.pct
            display = Display.flex
            alignItems = Align.center
            justifyContent = JustifyContent.center
            flex(.0, .0, FlexBasis.auto)
            userSelect = UserSelect.none
            fontWeight = FontWeight.w600
            fontSize = 18.px
            color = Color.white
            backgroundColor = Color("#777777")
            backgroundSize = "cover"
            backgroundPosition = "center"
            props.user.avatarUrl?.let { backgroundImage = Image("url(\"$it\")") }
        }

        props.user.avatarUrl ?: props.user.displayName.firstOrNull()?.let {
            +it.toString()
        }
    }
}
