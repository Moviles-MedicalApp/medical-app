package pe.com.smart.data.remote.appointment.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
    val id: Int,
    val patientId: Int,
    val doctorId: Int,
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val status: String
)