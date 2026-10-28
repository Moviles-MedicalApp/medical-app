package pe.com.smart.presentation.doctors.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.repository.DoctorRepository

class DoctorDetailViewModel  : ViewModel() {

    private val repository: DoctorRepository =
        ApiProvider.doctorRepository

    private val _uiState = MutableStateFlow(DoctorDetailUiState())
    val uiState: StateFlow<DoctorDetailUiState> =
        _uiState.asStateFlow()

    fun loadDoctor(id: Int) {
        viewModelScope.launch {
            _uiState.value = DoctorDetailUiState(isLoading = true)

            repository.getDoctorById(id)
                .onSuccess { doctor ->
                    _uiState.value = DoctorDetailUiState(
                        doctor = doctor
                    )
                }
                .onFailure { exception ->
                    _uiState.value = DoctorDetailUiState(
                        error = exception.message
                            ?: "No se pudo cargar el médico."
                    )
                }
        }
    }

    fun deleteDoctor(
        onSuccess: () -> Unit
    ) {
        val doctor = _uiState.value.doctor ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                error = null
            )

            repository.deleteDoctor(doctor.id)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message
                            ?: "No se pudo eliminar el médico."
                    )
                }
        }
    }
}