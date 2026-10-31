package pe.com.smart.data.remote.schedule.dto

import kotlinx.serialization.Serializable

@Serializable
data class MedicalScheduleRequestDto(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val appointmentDuration: Int,
    val doctorId: Int
)
