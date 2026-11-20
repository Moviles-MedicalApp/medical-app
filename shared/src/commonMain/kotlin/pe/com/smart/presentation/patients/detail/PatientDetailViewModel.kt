package pe.com.smart.presentation.patients.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.usecase.AppointmentUseCase
import pe.com.smart.domain.usecase.PatientUseCase

class PatientDetailViewModel(
    private val patientId: Int,
    private val repository: PatientUseCase,
    private val appointmentUseCase: AppointmentUseCase
) : ViewModel() {

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

                    val appointments: List<Appointment> = appointmentUseCase
                        .getAppointments()
                        .getOrDefault(emptyList<Appointment>())
                        .filter { it.patientId == patientId }
                    val upcoming = appointments
                        .filter { it.status == "SCHEDULED" || it.status == "CONFIRMED" }
                        .sortedWith(compareBy<Appointment> { it.appointmentDate }.thenBy { it.startTime })
                    val history = appointments
                        .filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
                        .sortedWith(compareByDescending<Appointment> { it.appointmentDate }.thenByDescending { it.startTime })

                    _uiState.value =
                        _uiState.value.copy(
                            patient = patient,
                            upcomingAppointments = upcoming,
                            appointmentHistory = history,
                            isLoading = false,
                            error = null
                        )
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
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
                            isDeleting = false
                        )

                    UiMessageManager.success(
                        title = "Paciente eliminado",
                        message = "El registro fue eliminado correctamente."
                    )

                    onSuccess()
                }
                .onFailure { exception ->

                    val errorMessage =
                        exception.message
                            ?: "No se pudo eliminar el paciente."

                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting = false,
                            error = errorMessage
                        )

                    UiMessageManager.error(
                        title = "No se pudo eliminar",
                        message = errorMessage
                    )
                }
        }
    }
}
