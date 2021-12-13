package to.bnt.draw.shared.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val format = Json { classDiscriminator = "action" }

@Serializable
sealed class Action {
    companion object {
        fun fromJson(json: String): Action = format.decodeFromString(json)
    }

    fun toJson() = format.encodeToString(this)
}

@Serializable
@SerialName("addFigures")
class AddFigures(val figures: List<Figure>) : Action()

@Serializable
@SerialName("removeFigure")
class RemoveFigure(val figureId: Int) : Action()

@Serializable
@SerialName("connectedUsers")
class ConnectedUsers(val users: List<User>) : Action()

@Serializable
@SerialName("userConnected")
class UserConnected(val user: User) : Action()

@Serializable
@SerialName("userDisconnected")
class UserDisconnected(val userId: Int) : Action()
