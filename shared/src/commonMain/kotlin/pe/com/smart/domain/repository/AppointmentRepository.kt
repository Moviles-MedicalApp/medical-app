package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Appointment

interface AppointmentRepository {
    suspend fun getAppointments(): Result<List<Appointment>>
    suspend fun create(reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int): Result<Appointment>
    suspend fun update(id: Int, reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int): Result<Appointment>
    suspend fun confirm(id: Int): Result<Appointment>
    suspend fun complete(id: Int): Result<Appointment>
    suspend fun cancel(id: Int): Result<Appointment>
}
