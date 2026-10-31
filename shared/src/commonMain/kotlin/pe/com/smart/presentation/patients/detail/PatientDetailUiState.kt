package pe.com.smart.presentation.patients.detail

import pe.com.smart.domain.model.Patient
import pe.com.smart.domain.model.Appointment

data class PatientDetailUiState(
    val patient: Patient? = null,
    val upcomingAppointments: List<Appointment> = emptyList(),
    val appointmentHistory: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)
