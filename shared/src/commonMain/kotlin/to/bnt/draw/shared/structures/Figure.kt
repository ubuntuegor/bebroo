package to.bnt.draw.shared.structures

import kotlinx.serialization.Serializable

@Serializable
data class Figure(val id: Int?, val drawingData: String, val color: String, val strokeWidth: Int)
