package pe.com.smart.presentation.patients.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.repository.PatientRepository

class PatientDetailViewModel(
    private val patientId: Int
) : ViewModel() {

    private val repository: PatientRepository =
        ApiProvider.patientRepository

    private val _uiState =
        MutableStateFlow(
            PatientDetailUiState()
        )

    val uiState: StateFlow<PatientDetailUiState> =
        _uiState.asStateFlow()

    fun loadPatient() {

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
                .getPatientById(
                    patientId
                )
                .onSuccess { patient ->

                    _uiState.value =
                        _uiState.value.copy(
                            patient =
                                patient,
                            isLoading =
                                false,
                            error =
                                null
                        )
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading =
                                false,

                            error =
                                exception.message
                                    ?: "No se pudo cargar el paciente."
                        )
                }
        }
    }

    fun deletePatient(
        onSuccess: () -> Unit
    ) {

        if (_uiState.value.isDeleting) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isDeleting = true,
                    error = null
                )

            repository
                .deletePatient(
                    patientId
                )
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting =
                                false
                        )

                    onSuccess()
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting =
                                false,

                            error =
                                exception.message
                                    ?: "No se pudo eliminar el paciente."
                        )
                }
        }
    }
}