package pe.com.smart.data.remote.patient

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
import pe.com.smart.data.remote.patient.dto.PatientDto
import pe.com.smart.data.remote.patient.dto.PatientRequestDto

class PatientApiService(
    private val client: HttpClient
) {

    suspend fun getPatients(): List<PatientDto> {

        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/patients"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudieron obtener los pacientes."
        )

        return response.body()
    }

    suspend fun getPatientById(
        id: Int
    ): PatientDto {

        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/patients/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo obtener el paciente."
        )

        return response.body()
    }

    suspend fun createPatient(
        request: PatientRequestDto
    ) {

        val response = client.post(
            "${ApiConstants.MEDICAL_APPOINTMENT}/patients"
        ) {

            contentType(
                ContentType.Application.Json
            )

            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo registrar el paciente."
        )
    }

    suspend fun updatePatient(
        id: Int,
        request: PatientRequestDto
    ) {

        val response = client.put(
            "${ApiConstants.MEDICAL_APPOINTMENT}/patients/$id"
        ) {

            contentType(
                ContentType.Application.Json
            )

            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo actualizar el paciente."
        )
    }

    suspend fun deletePatient(
        id: Int
    ) {

        val response = client.delete(
            "${ApiConstants.MEDICAL_APPOINTMENT}/patients/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo eliminar el paciente."
        )
    }
}