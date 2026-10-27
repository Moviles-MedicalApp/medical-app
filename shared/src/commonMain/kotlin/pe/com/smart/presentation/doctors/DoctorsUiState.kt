package pe.com.smart.presentation.doctors

import pe.com.smart.domain.model.Doctor

data class DoctorsUiState(
    val doctors: List<Doctor> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)