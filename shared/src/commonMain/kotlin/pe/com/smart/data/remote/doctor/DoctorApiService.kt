package pe.com.smart.data.remote.doctor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.com.smart.core.network.ApiConstants
import pe.com.smart.core.network.ensureSuccess
import pe.com.smart.data.remote.doctor.dt.DoctorDto
import pe.com.smart.data.remote.doctor.dt.DoctorRequestDto

class DoctorApiService(
    private val client: HttpClient
) {

    suspend fun getDoctors(): List<DoctorDto> {
        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/doctors"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudieron obtener los médicos."
        )

        return response.body()
    }

    suspend fun getDoctorById(id: Int): DoctorDto {
        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/doctors/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo obtener el médico."
        )

        return response.body()
    }

    suspend fun createDoctor(
        request: DoctorRequestDto
    ) {
        val response = client.post(
            "${ApiConstants.MEDICAL_APPOINTMENT}/doctors"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo registrar el médico."
        )
    }

    suspend fun updateDoctor(
        id: Int,
        request: DoctorRequestDto
    ) {
        val response = client.put(
            "${ApiConstants.MEDICAL_APPOINTMENT}/doctors/$id"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo actualizar el médico."
        )
    }

    suspend fun deleteDoctor(id: Int) {
        val response = client.delete(
            "${ApiConstants.MEDICAL_APPOINTMENT}/doctors/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo eliminar el médico."
        )
    }
}