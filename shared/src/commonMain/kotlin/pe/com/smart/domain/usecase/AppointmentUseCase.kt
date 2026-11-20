package pe.com.smart.domain.usecase

import pe.com.smart.domain.repository.AppointmentRepository

class AppointmentUseCase(private val repository: AppointmentRepository) {
    suspend fun getMyAppointments() = repository.getMyAppointments()
    suspend fun createMyAppointment(reason: String, date: String, start: String, end: String, doctorId: Int) = repository.createMyAppointment(reason, date, start, end, doctorId)
    suspend fun cancelMyAppointment(id: Int) = repository.cancelMyAppointment(id)
    suspend fun getMyDoctorAgenda(status: String? = null, dateFrom: String? = null, dateTo: String? = null) = repository.getMyDoctorAgenda(status, dateFrom, dateTo)
    suspend fun completeMyDoctorAppointment(id: Int) = repository.completeMyDoctorAppointment(id)
    suspend fun getAppointments() = repository.getAppointments()
    suspend fun create(reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int) = repository.create(reason, date, start, end, patientId, doctorId)
    suspend fun update(id: Int, reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int) = repository.update(id, reason, date, start, end, patientId, doctorId)
    suspend fun confirm(id: Int) = repository.confirm(id)
    suspend fun complete(id: Int) = repository.complete(id)
    suspend fun cancel(id: Int) = repository.cancel(id)
}
