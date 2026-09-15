package pe.com.smart.presentation.appointments

// ViewModel de Android que mantiene los datos de la pantalla
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// Herramientas para manejar el estado de la pantalla
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Lugar donde tenemos conectado nuestro repositorio de citas
import pe.com.smart.data.remote.ApiProvider

// Interfaz del repositorio de citas
import pe.com.smart.domain.repository.AppointmentRepository


// ViewModel encargado de manejar la lógica de la pantalla de citas
class AppointmentsViewModel : ViewModel() {

    // Obtenemos el repositorio de citas desde ApiProvider
    private val repository: AppointmentRepository =
        ApiProvider.appointmentRepository


    // Estado interno de la pantalla.
    // Solo el ViewModel puede modificarlo.
    private val _uiState =
        MutableStateFlow(
            AppointmentsUiState()
        )


    // Estado público que observará la pantalla
    val uiState: StateFlow<AppointmentsUiState> =
        _uiState.asStateFlow()


    // Función que carga todas las citas desde la API
    fun loadAppointments() {

        // Evita realizar otra petición si ya está cargando
        if (_uiState.value.isLoading) {
            return
        }

        // Ejecutamos la petición sin bloquear la aplicación
        viewModelScope.launch {

            // Indicamos que comenzó la carga
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )


            // Pedimos las citas al repositorio
            repository
                .getAppointments()

                // Si la petición fue correcta
                .onSuccess { appointments ->

                    // Guardamos las citas para mostrarlas en pantalla
                    _uiState.value =
                        _uiState.value.copy(
                            appointments = appointments,
                            isLoading = false,
                            errorMessage = null
                        )
                }

                // Si ocurrió algún error
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,

                            // Mostramos el mensaje que envía el servidor
                            // o uno por defecto
                            errorMessage =
                                exception.message
                                    ?: "No se pudieron cargar las citas."
                        )
                }
        }
    }
}