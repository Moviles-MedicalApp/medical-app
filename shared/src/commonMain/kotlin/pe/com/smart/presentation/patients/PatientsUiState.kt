package pe.com.smart.presentation.patients

import pe.com.smart.domain.model.Patient

data class PatientsUiState(
    val patients: List<Patient> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)