package pe.com.smart.data.remote.appointment

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.com.smart.core.network.ApiConstants
import pe.com.smart.core.network.ensureSuccess
import pe.com.smart.data.remote.appointment.dto.AppointmentDto
import pe.com.smart.data.remote.appointment.dto.AppointmentRequestDto
import pe.com.smart.data.remote.appointment.dto.AppointmentStatusDto
import pe.com.smart.data.remote.appointment.dto.CreateOwnAppointmentRequest
import pe.com.smart.data.remote.appointment.dto.DoctorAgendaDto
import io.ktor.client.request.parameter

class AppointmentApiService(private val client: HttpClient) {
    private val endpoint = "${ApiConstants.MEDICAL_APPOINTMENT}/appointments"
    suspend fun getAppointments(): List<AppointmentDto> = client.get(endpoint).also { it.ensureSuccess("No se pudieron obtener las citas.") }.body()
    suspend fun getById(id: Int): AppointmentDto = client.get("$endpoint/$id").also { it.ensureSuccess("No se pudo obtener la cita.") }.body()
    suspend fun create(request: AppointmentRequestDto): AppointmentDto = client.post(endpoint) { contentType(ContentType.Application.Json); setBody(request) }.also { it.ensureSuccess("No se pudo registrar la cita.") }.body()
    suspend fun update(id: Int, request: AppointmentRequestDto): AppointmentDto = client.put("$endpoint/$id") { contentType(ContentType.Application.Json); setBody(request) }.also { it.ensureSuccess("No se pudo actualizar la cita.") }.body()
    suspend fun changeStatus(id: Int, action: String): AppointmentStatusDto = client.patch("$endpoint/$id/$action").also { it.ensureSuccess("No se pudo actualizar la cita.") }.body()
    suspend fun getMyAppointments(): List<AppointmentDto> = client.get("$endpoint/me").also { it.ensureSuccess("No se pudieron obtener tus citas.") }.body()
    suspend fun createMyAppointment(request: CreateOwnAppointmentRequest): AppointmentDto = client.post("$endpoint/me") { contentType(ContentType.Application.Json); setBody(request) }.also { it.ensureSuccess("No se pudo registrar la cita.") }.body()
    suspend fun cancelMyAppointment(id: Int): AppointmentStatusDto = client.patch("$endpoint/me/$id/cancel").also { it.ensureSuccess("No se pudo cancelar la cita.") }.body()
    suspend fun getDoctorAgenda(status: String? = null, dateFrom: String? = null, dateTo: String? = null): DoctorAgendaDto = client.get("$endpoint/doctor/me/agenda") {
        status?.let { parameter("status", it) }
        dateFrom?.let { parameter("dateFrom", it) }
        dateTo?.let { parameter("dateTo", it) }
    }.also { it.ensureSuccess("No se pudo obtener tu agenda.") }.body()
    suspend fun completeMyDoctorAppointment(id: Int): AppointmentStatusDto = client.patch("$endpoint/doctor/me/$id/complete").also { it.ensureSuccess("No se pudo completar la cita.") }.body()
}
