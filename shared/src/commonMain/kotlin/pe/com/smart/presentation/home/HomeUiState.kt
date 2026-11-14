
package pe.com.smart.presentation.home

/**
 * Estado principal del Dashboard.
 *
 * Los indicadores se calculan según
 * el rol del usuario autenticado.
 */
data class HomeUiState(
    val username: String = "",
    val role: HomeRole = HomeRole.UNKNOWN,

    // Indicadores de citas.
    val todayAppointments: Int = 0,
    val upcomingAppointments: Int = 0,
    val activeAppointments: Int = 0,

    // Indicadores administrativos.
    val patients: Int = 0,
    val doctors: Int = 0,
    val specialities: Int = 0,

    // Alertas administrativas.
    val unconfirmedAppointments: Int = 0,
    val doctorsWithoutSchedules: Int = 0,

    // Próxima cita.
    val nextAppointment: AppointmentSummary? = null,

    // Estado de carga.
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Información resumida de una cita.
 *
 * Compatible con NextAppointmentCard.
 */
data class AppointmentSummary(
    val id: Int,
    val time: String,
    val patientName: String,
    val speciality: String,
    val doctorName: String
)
