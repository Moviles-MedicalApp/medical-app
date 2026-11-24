package pe.com.smart.data.repository

import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.schedule.MedicalScheduleApiService
import pe.com.smart.data.remote.schedule.dto.MedicalScheduleRequestDto
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.domain.repository.MedicalScheduleRepository

class MedicalScheduleRepositoryImpl(
    private val apiService: MedicalScheduleApiService
) : MedicalScheduleRepository {
    override suspend fun getSchedules(): Result<List<MedicalSchedule>> = runCatching {
        apiService.getSchedules().map { it.toDomain() }
    }

    override suspend fun getSchedulesByDoctor(doctorId: Int): Result<List<MedicalSchedule>> = runCatching {
        apiService.getSchedulesByDoctor(doctorId).map { it.toDomain() }
    }

    override suspend fun createSchedule(dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int): Result<MedicalSchedule> = runCatching {
        apiService.createSchedule(MedicalScheduleRequestDto(dayOfWeek, startTime, endTime, appointmentDuration, doctorId)).toDomain()
    }

    override suspend fun updateSchedule(id: Int, dayOfWeek: String, startTime: String, endTime: String, appointmentDuration: Int, doctorId: Int): Result<MedicalSchedule> = runCatching {
        apiService.updateSchedule(id, MedicalScheduleRequestDto(dayOfWeek, startTime, endTime, appointmentDuration, doctorId)).toDomain()
    }

    override suspend fun deleteSchedule(id: Int): Result<Unit> = runCatching { apiService.deleteSchedule(id) }
}
