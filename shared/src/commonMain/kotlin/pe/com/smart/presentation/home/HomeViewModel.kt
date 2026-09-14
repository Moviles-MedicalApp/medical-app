package pe.com.smart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider

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
                val patients =
                    patientRepository
                        .getPatients()
                        .getOrThrow()

                /*
                 * =========================================
                 * ACTUALIZAMOS EL HOME
                 * =========================================
                 */
                _uiState.update {

                    it.copy(
                        username = "admin",

                        todayAppointments = 0,

                        patients = patients.size,

                        doctors = 3,

                        specialities = 4,

                        nextAppointment = null,

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