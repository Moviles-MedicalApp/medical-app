package pe.com.smart.presentation.doctors.detail

import pe.com.smart.domain.model.Doctor

data class DoctorDetailUiState (
    val doctor: Doctor? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleting: Boolean = false
)