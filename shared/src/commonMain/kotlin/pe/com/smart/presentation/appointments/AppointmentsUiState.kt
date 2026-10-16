package pe.com.smart.presentation.appointments

import pe.com.smart.domain.model.Appointment

// Representa el estado actual de la pantalla de citas
data class AppointmentsUiState(

    // Lista de citas que se mostrarán en pantalla
    val appointments: List<Appointment> = emptyList(),

    // Indica si la información está cargando
    val isLoading: Boolean = false,

    // Guarda un mensaje de error si algo falla
    val errorMessage: String? = null
)