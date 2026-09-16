package pe.com.smart.data.remote.speciality.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpecialityRequestDto(
    val name: String,
    val description: String,
    val active: Boolean = true
)
