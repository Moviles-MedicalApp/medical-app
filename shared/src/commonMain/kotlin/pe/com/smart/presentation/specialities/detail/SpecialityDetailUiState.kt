package pe.com.smart.presentation.specialities.detail

import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.Speciality

data class SpecialityDetailUiState(
    val speciality: Speciality? = null,
    val doctors: List<Doctor> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)
