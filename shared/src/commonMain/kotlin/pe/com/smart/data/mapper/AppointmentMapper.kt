package pe.com.smart.data.mapper

import pe.com.smart.data.remote.appointment.dto.AppointmentDto
import pe.com.smart.domain.model.Appointment

fun AppointmentDto.toDomain(): Appointment {
    return Appointment(
        id = id,
        patientId = patientId,
        doctorId = doctorId,
        reason = reason,
        appointmentDate = appointmentDate,
        startTime = startTime,
        endTime = endTime,
        status = status
    )
}