package pe.com.smart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Appointment

class HomeViewModel : ViewModel() {

    private val patientRepository =
        ApiProvider.patientRepository

    private val _uiState =
        MutableStateFlow(
            HomeUiState()
        )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {

                /*
                 * =========================================
                 * PACIENTES
                 * =========================================
                 *
                 * getPatients() devuelve Result<List<Patient>>
                 * por eso extraemos primero la lista.
                 */
                val patients = async { patientRepository.getPatients() }
                val doctors = async { ApiProvider.doctorRepository.getDoctors() }
                val appointments = async { ApiProvider.appointmentRepository.getAppointments() }
                val patientList = patients.await().getOrThrow()
                val doctorList = doctors.await().getOrThrow()
                val activeAppointments = appointments.await().getOrThrow()
                    .filter { it.status == "SCHEDULED" || it.status == "CONFIRMED" }
                    .sortedWith(compareBy<Appointment> { it.appointmentDate }.thenBy { it.startTime })
                val next = activeAppointments.firstOrNull()

                /*
                 * =========================================
                 * ACTUALIZAMOS EL HOME
                 * =========================================
                 */
                _uiState.update {

                    it.copy(
                        username = "admin",

                        todayAppointments = activeAppointments.size,

                        patients = patientList.size,

                        doctors = doctorList.size,

                        specialities = doctorList.map { it.specialityId }.distinct().size,

                        nextAppointment = next?.let { appointment ->
                            AppointmentSummary(
                                id = appointment.id,
                                time = "${appointment.appointmentDate} · ${appointment.timeRange}",
                                patientName = appointment.patientFullName,
                                speciality = appointment.specialityName,
                                doctorName = appointment.doctorFullName
                            )
                        },

                        isLoading = false,

                        error = null
                    )
                }

            } catch (exception: Exception) {

                _uiState.update {

                    it.copy(
                        isLoading = false,

                        error =
                            exception.message
                                ?: "No se pudo cargar el resumen."
                    )
                }
            }
        }
    }

    fun retry() {

        loadDashboard()
    }
}
