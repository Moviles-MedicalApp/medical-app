package pe.com.smart.presentation.patients.detail

import pe.com.smart.domain.model.Patient

data class PatientDetailUiState(
    val patient: Patient? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)