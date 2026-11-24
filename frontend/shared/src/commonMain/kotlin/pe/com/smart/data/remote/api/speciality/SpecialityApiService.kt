package pe.com.smart.data.remote.speciality

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
import pe.com.smart.data.remote.speciality.dto.SpecialityDto
import pe.com.smart.data.remote.speciality.dto.SpecialityRequestDto

class SpecialityApiService(
    private val client: HttpClient
) {

    suspend fun getSpecialities(): List<SpecialityDto> {

        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/specialities"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudieron obtener las especialidades."
        )

        return response.body()
    }

    suspend fun getSpecialityById(
        id: Int
    ): SpecialityDto {

        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/specialities/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo obtener la especialidad."
        )

        return response.body()
    }

    suspend fun createSpeciality(
        request: SpecialityRequestDto
    ) {

        val response = client.post(
            "${ApiConstants.MEDICAL_APPOINTMENT}/specialities"
        ) {

            contentType(
                ContentType.Application.Json
            )

            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo registrar la especialidad."
        )
    }

    suspend fun updateSpeciality(
        id: Int,
        request: SpecialityRequestDto
    ) {

        val response = client.put(
            "${ApiConstants.MEDICAL_APPOINTMENT}/specialities/$id"
        ) {

            contentType(
                ContentType.Application.Json
            )

            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo actualizar la especialidad."
        )
    }

    suspend fun deleteSpeciality(
        id: Int
    ) {

        val response = client.delete(
            "${ApiConstants.MEDICAL_APPOINTMENT}/specialities/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo eliminar la especialidad."
        )
    }
}
