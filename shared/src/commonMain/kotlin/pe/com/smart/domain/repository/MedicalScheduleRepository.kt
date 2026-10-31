package pe.com.smart.domain.repository

import pe.com.smart.domain.model.MedicalSchedule

interface MedicalScheduleRepository {
    suspend fun getSchedules(): Result<List<MedicalSchedule>>
    suspend fun getSchedulesByDoctor(doctorId: Int): Result<List<MedicalSchedule>>
    suspend fun createSchedule(dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int): Result<MedicalSchedule>
    suspend fun updateSchedule(id: Int, dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int): Result<MedicalSchedule>
    suspend fun deleteSchedule(id: Int): Result<Unit>
}
