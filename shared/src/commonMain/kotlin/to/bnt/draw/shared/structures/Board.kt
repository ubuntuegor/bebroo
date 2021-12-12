package to.bnt.draw.shared.structures

import kotlinx.serialization.Serializable

@Serializable
data class Board(
    val uuid: String,
    val name: String,
    val creator: User,
    val isPublic: Boolean,
    val timestamp: Long? = null,
    val contributors: List<User>? = null,
    val figures: List<Figure>? = null
)
