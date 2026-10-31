package pe.com.smart.data.remote.schedule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.com.smart.core.network.ApiConstants
import pe.com.smart.core.network.ensureSuccess
import pe.com.smart.data.remote.schedule.dto.MedicalScheduleDto
import pe.com.smart.data.remote.schedule.dto.MedicalScheduleRequestDto

class MedicalScheduleApiService(private val client: HttpClient) {
    suspend fun getSchedules(): List<MedicalScheduleDto> {
        val response = client.get("${ApiConstants.MEDICAL_APPOINTMENT}/doctor-schedules")
        response.ensureSuccess("No se pudieron obtener los horarios médicos.")
        return response.body()
    }

    suspend fun getSchedulesByDoctor(doctorId: Int): List<MedicalScheduleDto> {
        val response = client.get("${ApiConstants.MEDICAL_APPOINTMENT}/doctor-schedules/doctor/$doctorId")
        response.ensureSuccess("No se pudieron obtener los horarios del médico.")
        return response.body()
    }

    suspend fun createSchedule(request: MedicalScheduleRequestDto): MedicalScheduleDto {
        val response = client.post("${ApiConstants.MEDICAL_APPOINTMENT}/doctor-schedules") { contentType(ContentType.Application.Json); setBody(request) }
        response.ensureSuccess("No se pudo agregar el horario.")
        return response.body()
    }

    suspend fun updateSchedule(id: Int, request: MedicalScheduleRequestDto): MedicalScheduleDto {
        val response = client.put("${ApiConstants.MEDICAL_APPOINTMENT}/doctor-schedules/$id") { contentType(ContentType.Application.Json); setBody(request) }
        response.ensureSuccess("No se pudo actualizar el horario.")
        return response.body()
    }

    suspend fun deleteSchedule(id: Int) {
        val response = client.delete("${ApiConstants.MEDICAL_APPOINTMENT}/doctor-schedules/$id")
        response.ensureSuccess("No se pudo eliminar el horario.")
    }
}
