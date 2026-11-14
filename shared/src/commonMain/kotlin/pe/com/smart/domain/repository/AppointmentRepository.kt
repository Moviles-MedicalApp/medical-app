package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Appointment

interface AppointmentRepository {
    suspend fun getMyAppointments(): Result<List<Appointment>>
    suspend fun createMyAppointment(reason: String, date: String, start: String, end: String, doctorId: Int): Result<Appointment>
    suspend fun cancelMyAppointment(id: Int): Result<Appointment>
    suspend fun getMyDoctorAgenda(status: String? = null, dateFrom: String? = null, dateTo: String? = null): Result<List<Appointment>>
    suspend fun completeMyDoctorAppointment(id: Int): Result<Unit>
    suspend fun getAppointments(): Result<List<Appointment>>
    suspend fun create(reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int): Result<Appointment>
    suspend fun update(id: Int, reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int): Result<Appointment>
    suspend fun confirm(id: Int): Result<Appointment>
    suspend fun complete(id: Int): Result<Appointment>
    suspend fun cancel(id: Int): Result<Appointment>
}
