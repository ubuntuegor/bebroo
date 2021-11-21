package to.bnt.draw.shared.structures

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val displayName: String, val avatarUrl: String?)
