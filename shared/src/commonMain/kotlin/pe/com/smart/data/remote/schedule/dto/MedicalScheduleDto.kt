package pe.com.smart.data.remote.schedule.dto

import kotlinx.serialization.Serializable

@Serializable
data class MedicalScheduleDto(
    val id: Int? = null,
    val dayOfWeek: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val appointmentDuration: Int? = null,
    val doctorId: Int? = null,
    val doctorFullName: String? = null
)
