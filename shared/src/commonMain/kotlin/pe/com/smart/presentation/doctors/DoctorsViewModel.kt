package pe.com.smart.presentation.doctors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.repository.DoctorRepository

class DoctorsViewModel : ViewModel() {
    private val repository: DoctorRepository =
        ApiProvider.doctorRepository

    private val _uiState =
        MutableStateFlow(DoctorsUiState())

    val uiState: StateFlow<DoctorsUiState> =
        _uiState.asStateFlow()

    private var allDoctors: List<Doctor> = emptyList()

    fun loadDoctors() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getDoctors()
                .onSuccess { doctors ->
                    allDoctors = doctors
                    applyFilter(_uiState.value.searchQuery)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "No se pudieron cargar los médicos."
                    )
                }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = value
        )

        applyFilter(value)
    }

    private fun applyFilter(query: String) {
        val filtered: List<Doctor> =
            if (query.isBlank()) {
                allDoctors
            } else {
                allDoctors.filter { doctor ->
                    doctor.fullName.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            doctor.email.orEmpty().contains(
                                query,
                                ignoreCase = true
                            ) ||
                            doctor.phone.orEmpty().contains(
                                query,
                                ignoreCase = true
                            ) ||
                            doctor.specialityName.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }

        _uiState.value = _uiState.value.copy(
            doctors = filtered,
            isLoading = false,
            error = null
        )
    }
}
