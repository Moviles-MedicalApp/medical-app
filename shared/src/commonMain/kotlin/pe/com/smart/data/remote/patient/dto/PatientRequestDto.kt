package pe.com.smart.data.remote.patient.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatientRequestDto(
    val name: String,
    val lastName: String,
    val dni: String,
    val email: String,
    val phone: String
)