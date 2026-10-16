package pe.com.smart.data.remote.patient.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatientDto(
    val id: Int,
    val name: String,
    val lastName: String,
    val fullName: String,
    val dni: String,
    val email: String,
    val phone: String
)