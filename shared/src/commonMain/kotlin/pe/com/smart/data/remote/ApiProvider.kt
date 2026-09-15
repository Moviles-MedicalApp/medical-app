package pe.com.smart.data.remote

// API para iniciar sesión
import pe.com.smart.data.remote.auth.AuthApiService

// API para trabajar con pacientes
import pe.com.smart.data.remote.patient.PatientApiService

// API para trabajar con citas médicas
import pe.com.smart.data.remote.appointment.AppointmentApiService

// Implementación del repositorio de pacientes
import pe.com.smart.data.repository.PatientRepositoryImpl

// Implementación del repositorio de citas
import pe.com.smart.data.repository.AppointmentRepositoryImpl

// Interfaces de los repositorios
import pe.com.smart.domain.repository.PatientRepository
import pe.com.smart.domain.repository.AppointmentRepository


// ApiProvider centraliza las conexiones con las diferentes APIs del sistema
object ApiProvider {

    // Cliente HTTP que se utilizará para comunicarse con el servidor
    private val httpClient =
        HttpClientFactory.create()


    // -------------------------
    // AUTENTICACIÓN
    // -------------------------

    // Servicio encargado del inicio de sesión
    val authApiService =
        AuthApiService(httpClient)


    // -------------------------
    // PACIENTES
    // -------------------------

    // Servicio que realiza las peticiones HTTP de pacientes
    private val patientApiService =
        PatientApiService(httpClient)

    // Repositorio que utilizará la aplicación para trabajar con pacientes
    val patientRepository: PatientRepository =
        PatientRepositoryImpl(
            patientApiService
        )


    // -------------------------
    // CITAS MÉDICAS
    // -------------------------

    // Servicio que realiza las peticiones HTTP de citas
    private val appointmentApiService =
        AppointmentApiService(httpClient)

    // Repositorio que utilizará la aplicación para trabajar con las citas
    val appointmentRepository: AppointmentRepository =
        AppointmentRepositoryImpl(
            appointmentApiService
        )
}