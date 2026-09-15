package pe.com.smart.data.remote.appointment

// Cliente HTTP que permite comunicarnos con la API
import io.ktor.client.HttpClient

// Permite convertir la respuesta de la API al tipo que necesitamos
import io.ktor.client.call.body

// Métodos HTTP que usaremos
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put

// Permite enviar datos en el cuerpo de una petición
import io.ktor.client.request.setBody

// Indicamos que trabajamos con datos JSON
import io.ktor.http.ContentType
import io.ktor.http.contentType

// Dirección base de la API del proyecto
import pe.com.smart.core.network.ApiConstants

// Función del proyecto que verifica si la respuesta fue correcta
import pe.com.smart.core.network.ensureSuccess

// DTO que recibimos desde la API
import pe.com.smart.data.remote.appointment.dto.AppointmentDto

// DTO que enviamos al crear o actualizar una cita
import pe.com.smart.data.remote.appointment.dto.AppointmentRequestDto


// Servicio encargado de comunicarse con la API de citas
class AppointmentApiService(
    private val client: HttpClient
) {

    // Obtiene la lista completa de citas
    suspend fun getAppointments(): List<AppointmentDto> {

        // Realizamos una petición GET al endpoint de citas
        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/appointments"
        )

        // Verificamos que la respuesta de la API sea correcta
        response.ensureSuccess(
            defaultMessage = "No se pudieron obtener las citas."
        )

        // Convertimos la respuesta JSON en una lista de AppointmentDto
        return response.body()
    }


    // Obtiene una cita específica utilizando su ID
    suspend fun getAppointmentById(
        id: Int
    ): AppointmentDto {

        val response = client.get(
            "${ApiConstants.MEDICAL_APPOINTMENT}/appointments/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo obtener la cita."
        )

        return response.body()
    }


    // Registra una nueva cita médica
    suspend fun createAppointment(
        request: AppointmentRequestDto
    ) {

        val response = client.post(
            "${ApiConstants.MEDICAL_APPOINTMENT}/appointments"
        ) {

            // Indicamos que enviamos información en formato JSON
            contentType(
                ContentType.Application.Json
            )

            // Enviamos los datos de la nueva cita
            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo registrar la cita."
        )
    }


    // Actualiza una cita existente
    suspend fun updateAppointment(
        id: Int,
        request: AppointmentRequestDto
    ) {

        val response = client.put(
            "${ApiConstants.MEDICAL_APPOINTMENT}/appointments/$id"
        ) {

            contentType(
                ContentType.Application.Json
            )

            // Enviamos los nuevos datos de la cita
            setBody(request)
        }

        response.ensureSuccess(
            defaultMessage = "No se pudo actualizar la cita."
        )
    }


    // Elimina una cita utilizando su ID
    suspend fun deleteAppointment(
        id: Int
    ) {

        val response = client.delete(
            "${ApiConstants.MEDICAL_APPOINTMENT}/appointments/$id"
        )

        response.ensureSuccess(
            defaultMessage = "No se pudo eliminar la cita."
        )
    }
}