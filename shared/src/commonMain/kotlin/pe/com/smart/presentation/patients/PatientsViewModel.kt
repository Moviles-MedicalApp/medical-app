package pe.com.smart.presentation.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Patient
import pe.com.smart.domain.repository.PatientRepository

class PatientsViewModel : ViewModel() {

    private val repository: PatientRepository =
        ApiProvider.patientRepository

    private val _uiState =
        MutableStateFlow(
            PatientsUiState()
        )

    val uiState: StateFlow<PatientsUiState> =
        _uiState.asStateFlow()

    private var allPatients: List<Patient> =
        emptyList()

    /*
     * Ya no cargamos en init.
     *
     * PatientsScreen controla cuándo
     * debe actualizarse.
     */
    fun loadPatients() {

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
                .getPatients()
                .onSuccess { patients ->

                    allPatients =
                        patients

                    applyFilter(
                        query =
                            _uiState.value
                                .searchQuery
                    )
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error =
                                exception.message
                                    ?: "No se pudieron cargar los pacientes."
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

        val filteredPatients =
            if (query.isBlank()) {

                allPatients

            } else {

                allPatients.filter { patient ->

                    patient.fullName.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            patient.dni.contains(
                                query,
                                ignoreCase = true
                            ) ||
                            patient.email.contains(
                                query,
                                ignoreCase = true
                            )
                }
            }

        _uiState.value =
            _uiState.value.copy(
                patients =
                    filteredPatients,
                isLoading =
                    false,
                error =
                    null
            )
    }
}