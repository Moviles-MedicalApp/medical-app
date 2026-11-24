package pe.com.smart.presentation.specialities

import pe.com.smart.domain.model.Speciality

data class SpecialitiesUiState(
    val specialities: List<Speciality> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
