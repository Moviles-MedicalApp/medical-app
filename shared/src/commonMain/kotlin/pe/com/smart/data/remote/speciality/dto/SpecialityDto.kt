package pe.com.smart.data.remote.speciality.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpecialityDto(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("deleted")
    val deleted: Boolean? = false
)
