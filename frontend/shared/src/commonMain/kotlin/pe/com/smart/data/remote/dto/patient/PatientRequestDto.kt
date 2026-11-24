package pe.com.smart.data.remote.dto.patient

import kotlinx.serialization.Serializable

@Serializable
data class PatientRequestDto(
    val name: String,
    val lastName: String,
    val dni: String,
    val email: String,
    val phone: String
)