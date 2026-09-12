package pe.com.smart.presentation.home

data class HomeUiState(
    val username: String = "admin",
    val todayAppointments: Int = 0,
    val patients: Int = 4,
    val doctors: Int = 3,
    val specialities: Int = 4,
    val nextAppointment: AppointmentSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AppointmentSummary(
    val id: Int,
    val time: String,
    val patientName: String,
    val speciality: String,
    val doctorName: String
)