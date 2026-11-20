package pe.com.smart.domain.usecase

import pe.com.smart.domain.repository.MedicalScheduleRepository

class MedicalScheduleUseCase(private val repository: MedicalScheduleRepository) {
    suspend fun getSchedules() = repository.getSchedules()
    suspend fun getSchedulesByDoctor(doctorId: Int) = repository.getSchedulesByDoctor(doctorId)
    suspend fun createSchedule(dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int) = repository.createSchedule(dayOfWeek, startTime, endTime, appointmentDuration, doctorId)
    suspend fun updateSchedule(id: Int, dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int) = repository.updateSchedule(id, dayOfWeek, startTime, endTime, appointmentDuration, doctorId)
    suspend fun deleteSchedule(id: Int) = repository.deleteSchedule(id)
}
