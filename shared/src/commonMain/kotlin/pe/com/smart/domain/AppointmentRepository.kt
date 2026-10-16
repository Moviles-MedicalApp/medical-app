package pe.com.smart.domain.repository

import pe.com.smart.domain.model.Appointment

// Repository que define las acciones que podremos realizar con las citas médicas
interface AppointmentRepository {

    // Obtiene la lista completa de citas
    suspend fun getAppointments():
            Result<List<Appointment>>

    // Obtiene una cita específica según su ID
    suspend fun getAppointmentById(
        id: Int
    ): Result<Appointment>

    // Registra una nueva cita médica
    suspend fun createAppointment(
        patientId: Int, // ID del paciente
        doctorId: Int, // ID del médico
        reason: String, // Motivo de la cita
        appointmentDate: String, // Fecha de la cita
        startTime: String, // Hora de inicio
        endTime: String, // Hora de término
        status: String // Estado de la cita
    ): Result<Unit>

    // Actualiza los datos de una cita existente
    suspend fun updateAppointment(
        id: Int, // ID de la cita que se va a modificar
        patientId: Int,
        doctorId: Int,
        reason: String,
        appointmentDate: String,
        startTime: String,
        endTime: String,
        status: String
    ): Result<Unit>

    // Elimina una cita utilizando su ID
    suspend fun deleteAppointment(
        id: Int
    ): Result<Unit>
}