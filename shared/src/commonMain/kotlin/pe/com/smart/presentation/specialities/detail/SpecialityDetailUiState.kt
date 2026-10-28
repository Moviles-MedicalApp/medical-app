package pe.com.smart.presentation.specialities.detail

import pe.com.smart.domain.model.Speciality

data class SpecialityDetailUiState(
    val speciality: Speciality? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)
