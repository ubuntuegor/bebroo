package to.bnt.draw.shared.structures

import kotlinx.serialization.Serializable

@Serializable
data class Figure(var id: Long?, val drawingData: String, val color: String, val strokeWidth: Int)
