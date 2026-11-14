package pe.com.smart.data.remote.appointment.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
    val id: Int,
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val patientId: Int,
    val patientFullName: String = "",
    val patientDni: String = "",
    val doctorId: Int,
    val doctorFullName: String = "",
    val specialityId: Int? = null,
    val specialityName: String? = null
)

@Serializable
data class AppointmentRequestDto(
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val patientId: Int,
    val doctorId: Int
)

@Serializable
data class CreateOwnAppointmentRequest(
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val doctorId: Int
)

@Serializable
data class AppointmentStatusDto(val id: Int)

@Serializable
data class DoctorAgendaDto(
    val doctorId: Int,
    val doctorFullName: String = "",
    val status: String = "CONFIRMED",
    val dateFrom: String = "",
    val dateTo: String = "",
    val total: Int = 0,
    val appointments: List<AppointmentDto> = emptyList()
)
