package pe.com.smart.data.remote.speciality.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpecialityDto(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val deleted: Boolean? = false
)
