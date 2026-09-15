package pe.com.smart.data.repository

import pe.com.smart.data.mapper.toDomain
import pe.com.smart.data.remote.appointment.AppointmentApiService
import pe.com.smart.data.remote.appointment.dto.AppointmentRequestDto
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.repository.AppointmentRepository

// Implementación del repositorio de citas.
// Aquí conectamos la capa de dominio con la API.
class AppointmentRepositoryImpl(
    private val apiService: AppointmentApiService
) : AppointmentRepository {

    // Obtiene todas las citas desde la API
    override suspend fun getAppointments():
            Result<List<Appointment>> {

        return try {

            // Pedimos las citas a la API
            // y convertimos cada DTO al modelo Appointment
            val appointments =
                apiService
                    .getAppointments()
                    .map {
                        it.toDomain()
                    }

            // Si todo sale bien, devolvemos la lista
            Result.success(appointments)

        } catch (e: Exception) {

            // Si ocurre un error, lo devolvemos
            Result.failure(e)
        }
    }

    // Obtiene una cita específica según su ID
    override suspend fun getAppointmentById(
        id: Int
    ): Result<Appointment> {

        return try {

            // Buscamos la cita en la API
            // y la convertimos al modelo del dominio
            val appointment =
                apiService
                    .getAppointmentById(id)
                    .toDomain()

            Result.success(appointment)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // Registra una nueva cita médica
    override suspend fun createAppointment(
        patientId: Int,
        doctorId: Int,
        reason: String,
        appointmentDate: String,
        startTime: String,
        endTime: String,
        status: String
    ): Result<Unit> {

        return try {

            // Creamos el objeto que será enviado a la API
            val request =
                AppointmentRequestDto(
                    patientId = patientId,
                    doctorId = doctorId,
                    reason = reason,
                    appointmentDate = appointmentDate,
                    startTime = startTime,
                    endTime = endTime,
                    status = status
                )

            // Enviamos la nueva cita a la API
            apiService.createAppointment(
                request
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // Actualiza una cita existente
    override suspend fun updateAppointment(
        id: Int,
        patientId: Int,
        doctorId: Int,
        reason: String,
        appointmentDate: String,
        startTime: String,
        endTime: String,
        status: String
    ): Result<Unit> {

        return try {

            // Creamos los nuevos datos de la cita
            val request =
                AppointmentRequestDto(
                    patientId = patientId,
                    doctorId = doctorId,
                    reason = reason,
                    appointmentDate = appointmentDate,
                    startTime = startTime,
                    endTime = endTime,
                    status = status
                )

            // Mandamos la actualización a la API
            apiService.updateAppointment(
                id = id,
                request = request
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // Elimina una cita por su ID
    override suspend fun deleteAppointment(
        id: Int
    ): Result<Unit> {

        return try {

            // Le pedimos a la API que elimine la cita
            apiService.deleteAppointment(id)

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}