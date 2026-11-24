package pe.com.smart.data.remote.mapper

import pe.com.smart.data.remote.dto.schedule.MedicalScheduleDto
import pe.com.smart.domain.model.MedicalSchedule

fun MedicalScheduleDto.toDomain() = MedicalSchedule(
    id = id ?: 0,
    dayOfWeek = dayOfWeek.orEmpty(),
    startTime = startTime.orEmpty(),
    endTime = endTime.orEmpty(),
    appointmentDuration = appointmentDuration ?: 0,
    doctorId = doctorId ?: 0,
    doctorFullName = doctorFullName.orEmpty()
)
