package pe.com.smart.presentation.specialities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Speciality
import pe.com.smart.domain.repository.SpecialityRepository

class SpecialitiesViewModel : ViewModel() {

    private val repository: SpecialityRepository =
        ApiProvider.specialityRepository

    private val _uiState =
        MutableStateFlow(
            SpecialitiesUiState()
        )

    val uiState: StateFlow<SpecialitiesUiState> =
        _uiState.asStateFlow()

    private var allSpecialities: List<Speciality> =
        emptyList()

    fun loadSpecialities() {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

            repository
                .getSpecialities()
                .onSuccess { specialities ->
                    allSpecialities = specialities
                    applyFilter(
                        query = _uiState.value.searchQuery
                    )
                }
                .onFailure { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                                ?: "No se pudieron cargar las especialidades."
                        )
                }
        }
    }

    fun onSearchChange(
        value: String
    ) {
        _uiState.value =
            _uiState.value.copy(
                searchQuery = value
            )

        applyFilter(value)
    }

    private fun applyFilter(
        query: String
    ) {
        val filteredSpecialities =
            if (query.isBlank()) {
                allSpecialities
            } else {
                allSpecialities.filter { speciality ->
                    speciality.name.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            speciality.description.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }

        _uiState.value =
            _uiState.value.copy(
                specialities = filteredSpecialities,
                isLoading = false,
                error = null
            )
    }
}
