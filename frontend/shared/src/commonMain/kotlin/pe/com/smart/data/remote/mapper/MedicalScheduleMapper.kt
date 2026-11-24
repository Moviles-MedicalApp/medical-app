package pe.com.smart.data.mapper

import pe.com.smart.data.remote.schedule.dto.MedicalScheduleDto
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
