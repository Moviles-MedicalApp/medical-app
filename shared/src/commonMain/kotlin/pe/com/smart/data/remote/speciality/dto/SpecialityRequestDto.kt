package pe.com.smart.data.remote.speciality.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpecialityRequestDto(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("deleted")
    val deleted: Boolean = false
)
