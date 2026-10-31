package pe.com.smart.data.repository

import pe.com.smart.data.remote.appointment.AppointmentApiService
import pe.com.smart.data.remote.appointment.dto.AppointmentDto
import pe.com.smart.data.remote.appointment.dto.AppointmentRequestDto
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.repository.AppointmentRepository

class AppointmentRepositoryImpl(private val api: AppointmentApiService) : AppointmentRepository {
    override suspend fun getAppointments() = runCatching { api.getAppointments().map { it.toDomain() } }
    override suspend fun create(reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int) = runCatching { api.create(request(reason, date, start, end, patientId, doctorId)).toDomain() }
    override suspend fun update(id: Int, reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int) = runCatching { api.update(id, request(reason, date, start, end, patientId, doctorId)).toDomain() }
    override suspend fun confirm(id: Int) = runCatching { api.changeStatus(id, "confirm"); api.getById(id).toDomain() }
    override suspend fun complete(id: Int) = runCatching { api.changeStatus(id, "complete"); api.getById(id).toDomain() }
    override suspend fun cancel(id: Int) = runCatching { api.changeStatus(id, "cancel"); api.getById(id).toDomain() }
    private fun request(reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int) = AppointmentRequestDto(reason, date, "$start:00", "$end:00", patientId, doctorId)
}

private fun AppointmentDto.toDomain() = Appointment(id, reason, appointmentDate, startTime, endTime, status, patientId, patientFullName, patientDni, doctorId, doctorFullName, specialityId, specialityName.orEmpty())
