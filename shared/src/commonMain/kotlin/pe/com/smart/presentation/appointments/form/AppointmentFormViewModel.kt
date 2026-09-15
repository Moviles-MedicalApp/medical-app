package pe.com.smart.presentation.appointments.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Permite mostrar mensajes de éxito o error
import pe.com.smart.core.ui.message.UiMessageManager

// Nos da acceso al repositorio de citas
import pe.com.smart.data.remote.ApiProvider

// Interfaz del repositorio de citas
import pe.com.smart.domain.repository.AppointmentRepository


/*
 * =====================================================
 * VIEWMODEL DEL FORMULARIO DE CITAS
 * =====================================================
 *
 * Este ViewModel se encarga de:
 *
 * - Crear una nueva cita
 * - Editar una cita existente
 * - Cargar los datos de una cita
 * - Validar los campos
 * - Guardar los cambios
 */
class AppointmentFormViewModel(

    // Si appointmentId es null:
    // estamos creando una cita.
    //
    // Si tiene un número:
    // estamos editando una cita existente.
    private val appointmentId: Int?

) : ViewModel() {


    /*
     * =====================================================
     * REPOSITORIO
     * =====================================================
     *
     * Aquí obtenemos acceso a las operaciones
     * relacionadas con las citas.
     */
    private val repository: AppointmentRepository =
        ApiProvider.appointmentRepository


    /*
     * =====================================================
     * ESTADO DEL FORMULARIO
     * =====================================================
     */

    private val _uiState =
        MutableStateFlow(
            AppointmentFormUiState()
        )

    // La pantalla observará este estado
    val uiState: StateFlow<AppointmentFormUiState> =
        _uiState.asStateFlow()


    /*
     * Nos permite saber fácilmente si
     * estamos creando o editando.
     */
    val isEditing: Boolean
        get() = appointmentId != null


    /*
     * =====================================================
     * DATOS ORIGINALES
     * =====================================================
     *
     * Se utilizan para detectar si el usuario
     * realmente modificó algún dato.
     */

    private var originalPatientId: String = ""
    private var originalDoctorId: String = ""
    private var originalReason: String = ""
    private var originalDate: String = ""
    private var originalStartTime: String = ""
    private var originalEndTime: String = ""
    private var originalStatus: String = "PROGRAMADA"


    /*
     * =====================================================
     * INICIO
     * =====================================================
     *
     * Si recibimos un ID significa que queremos
     * editar una cita, por eso cargamos sus datos.
     */
    init {

        if (appointmentId != null) {

            loadAppointment(
                appointmentId
            )
        }
    }


    /*
     * =====================================================
     * CARGAR CITA
     * =====================================================
     */
    private fun loadAppointment(
        id: Int
    ) {

        viewModelScope.launch {

            // Activamos el indicador de carga
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null
                )


            // Buscamos la cita por su ID
            repository
                .getAppointmentById(id)

                .onSuccess { appointment ->

                    /*
                     * Guardamos los datos originales.
                     * Después los utilizaremos para saber
                     * si el usuario realizó cambios.
                     */
                    originalPatientId =
                        appointment.patientId.toString()

                    originalDoctorId =
                        appointment.doctorId.toString()

                    originalReason =
                        appointment.reason

                    originalDate =
                        appointment.appointmentDate

                    originalStartTime =
                        appointment.startTime

                    originalEndTime =
                        appointment.endTime

                    originalStatus =
                        appointment.status


                    /*
                     * Colocamos los datos de la cita
                     * dentro del formulario.
                     */
                    _uiState.value =
                        _uiState.value.copy(

                            patientId =
                                appointment.patientId.toString(),

                            doctorId =
                                appointment.doctorId.toString(),

                            reason =
                                appointment.reason,

                            appointmentDate =
                                appointment.appointmentDate,

                            startTime =
                                appointment.startTime,

                            endTime =
                                appointment.endTime,

                            status =
                                appointment.status,

                            isLoading =
                                false,

                            hasChanges =
                                false,

                            error =
                                null
                        )
                }

                .onFailure { exception ->

                    val message =
                        exception.message
                            ?: "No se pudo cargar la cita."

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = message
                        )

                    UiMessageManager.error(
                        title = "No se pudo cargar",
                        message = message
                    )
                }
        }
    }


    /*
     * =====================================================
     * CAMBIAR PACIENTE
     * =====================================================
     */
    fun onPatientIdChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(

                // Permitimos solamente números
                patientId =
                    value.filter {
                        it.isDigit()
                    },

                patientIdError =
                    null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR MÉDICO
     * =====================================================
     */
    fun onDoctorIdChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(

                // Permitimos solamente números
                doctorId =
                    value.filter {
                        it.isDigit()
                    },

                doctorIdError =
                    null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR MOTIVO
     * =====================================================
     */
    fun onReasonChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                reason = value,
                reasonError = null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR FECHA
     * =====================================================
     */
    fun onDateChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                appointmentDate = value,
                appointmentDateError = null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR HORA DE INICIO
     * =====================================================
     */
    fun onStartTimeChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                startTime = value,
                startTimeError = null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR HORA DE FIN
     * =====================================================
     */
    fun onEndTimeChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                endTime = value,
                endTimeError = null
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * CAMBIAR ESTADO
     * =====================================================
     *
     * Ejemplos:
     * PROGRAMADA
     * ATENDIDA
     * CANCELADA
     */
    fun onStatusChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                status = value
            )

        updateHasChanges()
    }


    /*
     * =====================================================
     * DETECTAR CAMBIOS
     * =====================================================
     */
    private fun updateHasChanges() {

        val state =
            _uiState.value


        val hasChanges =
            if (appointmentId == null) {

                /*
                 * Si estamos creando una cita,
                 * hay cambios si el usuario ya escribió
                 * información en algún campo.
                 */
                state.patientId.isNotBlank() ||
                        state.doctorId.isNotBlank() ||
                        state.reason.isNotBlank() ||
                        state.appointmentDate.isNotBlank() ||
                        state.startTime.isNotBlank() ||
                        state.endTime.isNotBlank()

            } else {

                /*
                 * Si estamos editando,
                 * comparamos con los datos originales.
                 */
                state.patientId != originalPatientId ||
                        state.doctorId != originalDoctorId ||
                        state.reason != originalReason ||
                        state.appointmentDate != originalDate ||
                        state.startTime != originalStartTime ||
                        state.endTime != originalEndTime ||
                        state.status != originalStatus
            }


        _uiState.value =
            state.copy(
                hasChanges = hasChanges
            )
    }


    /*
     * =====================================================
     * GUARDAR CITA
     * =====================================================
     */
    fun saveAppointment(
        onSuccess: () -> Unit
    ) {

        // Evita guardar varias veces al mismo tiempo
        if (_uiState.value.isSaving) {
            return
        }


        // Primero verificamos que los datos sean correctos
        if (!validate()) {
            return
        }


        val state =
            _uiState.value


        /*
         * Convertimos los IDs escritos como texto
         * a números enteros.
         */
        val patientId =
            state.patientId.toIntOrNull()
                ?: return

        val doctorId =
            state.doctorId.toIntOrNull()
                ?: return


        viewModelScope.launch {

            // Indicamos que se está guardando
            _uiState.value =
                state.copy(
                    isSaving = true,
                    error = null
                )


            /*
             * Si appointmentId es null:
             * creamos una cita.
             *
             * Si existe:
             * actualizamos la cita.
             */
            val result =
                if (appointmentId == null) {

                    repository.createAppointment(

                        patientId =
                            patientId,

                        doctorId =
                            doctorId,

                        reason =
                            state.reason.trim(),

                        appointmentDate =
                            state.appointmentDate.trim(),

                        startTime =
                            state.startTime.trim(),

                        endTime =
                            state.endTime.trim(),

                        status =
                            state.status
                    )

                } else {

                    repository.updateAppointment(

                        id =
                            appointmentId,

                        patientId =
                            patientId,

                        doctorId =
                            doctorId,

                        reason =
                            state.reason.trim(),

                        appointmentDate =
                            state.appointmentDate.trim(),

                        startTime =
                            state.startTime.trim(),

                        endTime =
                            state.endTime.trim(),

                        status =
                            state.status
                    )
                }


            /*
             * =================================================
             * SI SE GUARDÓ CORRECTAMENTE
             * =================================================
             */
            result.onSuccess {

                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        hasChanges = false,
                        error = null
                    )


                if (appointmentId == null) {

                    UiMessageManager.success(
                        title = "Cita registrada",
                        message = "La cita fue guardada correctamente."
                    )

                } else {

                    UiMessageManager.success(
                        title = "Cita actualizada",
                        message = "Los cambios se guardaron correctamente."
                    )
                }


                // Regresamos a la pantalla anterior
                onSuccess()
            }


            /*
             * =================================================
             * SI OCURRIÓ UN ERROR
             * =================================================
             */
            result.onFailure { exception ->

                val message =
                    exception.message
                        ?: if (appointmentId == null) {
                            "No se pudo registrar la cita."
                        } else {
                            "No se pudo actualizar la cita."
                        }


                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        error = null
                    )


                UiMessageManager.error(

                    title =
                        if (appointmentId == null) {
                            "No se pudo registrar"
                        } else {
                            "No se pudo actualizar"
                        },

                    message =
                        message
                )
            }
        }
    }


    /*
     * =====================================================
     * VALIDAR FORMULARIO
     * =====================================================
     */
    private fun validate(): Boolean {

        val state =
            _uiState.value


        /*
         * VALIDAR PACIENTE
         */
        val patientIdError =
            when {

                state.patientId.isBlank() ->
                    "Ingresa el paciente"

                state.patientId.toIntOrNull() == null ->
                    "Paciente no válido"

                else ->
                    null
            }


        /*
         * VALIDAR MÉDICO
         */
        val doctorIdError =
            when {

                state.doctorId.isBlank() ->
                    "Ingresa el médico"

                state.doctorId.toIntOrNull() == null ->
                    "Médico no válido"

                else ->
                    null
            }


        /*
         * VALIDAR MOTIVO
         */
        val reasonError =
            if (
                state.reason.trim().isBlank()
            ) {
                "Ingresa el motivo de la cita"
            } else {
                null
            }


        /*
         * VALIDAR FECHA
         */
        val dateError =
            if (
                state.appointmentDate.trim().isBlank()
            ) {
                "Ingresa la fecha"
            } else {
                null
            }


        /*
         * VALIDAR HORA DE INICIO
         */
        val startTimeError =
            if (
                state.startTime.trim().isBlank()
            ) {
                "Ingresa la hora de inicio"
            } else {
                null
            }


        /*
         * VALIDAR HORA DE FIN
         */
        val endTimeError =
            if (
                state.endTime.trim().isBlank()
            ) {
                "Ingresa la hora de fin"
            } else {
                null
            }


        /*
         * Guardamos los mensajes de error
         * para mostrarlos debajo de cada campo.
         */
        _uiState.value =
            state.copy(

                patientIdError =
                    patientIdError,

                doctorIdError =
                    doctorIdError,

                reasonError =
                    reasonError,

                appointmentDateError =
                    dateError,

                startTimeError =
                    startTimeError,

                endTimeError =
                    endTimeError
            )


        /*
         * Si todos son null,
         * significa que el formulario es válido.
         */
        return (
                patientIdError == null &&
                        doctorIdError == null &&
                        reasonError == null &&
                        dateError == null &&
                        startTimeError == null &&
                        endTimeError == null
                )
    }
}