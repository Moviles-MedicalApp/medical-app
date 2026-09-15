package pe.com.smart.presentation.patients.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.repository.PatientRepository

class PatientFormViewModel(
    private val patientId: Int?
) : ViewModel() {

    private val repository: PatientRepository =
        ApiProvider.patientRepository

    private val _uiState =
        MutableStateFlow(
            PatientFormUiState()
        )

    val uiState: StateFlow<PatientFormUiState> =
        _uiState.asStateFlow()

    val isEditing: Boolean
        get() = patientId != null

    /*
     * =====================================================
     * VALORES ORIGINALES
     * =====================================================
     *
     * Permiten saber si realmente existen cambios.
     */
    private var originalName: String = ""
    private var originalLastName: String = ""
    private var originalDni: String = ""
    private var originalEmail: String = ""
    private var originalPhone: String = ""

    init {

        if (patientId != null) {

            loadPatient(
                patientId
            )
        }
    }

    /*
     * =====================================================
     * CARGAR PACIENTE
     * =====================================================
     */

    private fun loadPatient(
        id: Int
    ) {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

            repository
                .getPatientById(id)
                .onSuccess { patient ->

                    /*
                     * Guardamos referencia original.
                     */
                    originalName =
                        patient.name

                    originalLastName =
                        patient.lastName

                    originalDni =
                        patient.dni

                    originalEmail =
                        patient.email

                    originalPhone =
                        patient.phone

                    _uiState.value =
                        _uiState.value.copy(
                            name =
                                patient.name,

                            lastName =
                                patient.lastName,

                            dni =
                                patient.dni,

                            email =
                                patient.email,

                            phone =
                                patient.phone,

                            hasChanges =
                                false,

                            isLoading =
                                false,

                            error =
                                null
                        )
                }
                .onFailure { exception ->

                    val errorMessage =
                        exception.message
                            ?: "No se pudo cargar el paciente."

                    _uiState.value =
                        _uiState.value.copy(
                            /*
                             * CORRECCIÓN:
                             * aquí corresponde isLoading.
                             */
                            isLoading =
                                false,

                            error =
                                errorMessage
                        )

                    UiMessageManager.error(
                        title =
                            "No se pudo cargar",

                        message =
                            errorMessage
                    )
                }
        }
    }

    /*
     * =====================================================
     * NOMBRE
     * =====================================================
     */

    fun onNameChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                name =
                    value,

                nameError =
                    null
            )

        updateHasChanges()
    }

    /*
     * =====================================================
     * APELLIDO
     * =====================================================
     */

    fun onLastNameChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                lastName =
                    value,

                lastNameError =
                    null
            )

        updateHasChanges()
    }

    /*
     * =====================================================
     * DNI
     * =====================================================
     */

    fun onDniChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                dni =
                    value
                        .filter {
                            it.isDigit()
                        }
                        .take(8),

                dniError =
                    null
            )

        updateHasChanges()
    }

    /*
     * =====================================================
     * CORREO
     * =====================================================
     */

    fun onEmailChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                email =
                    value,

                emailError =
                    null
            )

        updateHasChanges()
    }

    /*
     * =====================================================
     * TELÉFONO
     * =====================================================
     */

    fun onPhoneChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                phone =
                    value
                        .filter {
                            it.isDigit()
                        }
                        .take(9),

                phoneError =
                    null
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
            if (patientId == null) {

                /*
                 * Nuevo paciente:
                 * existe cambio si algún campo
                 * contiene información.
                 */
                state.name.isNotBlank() ||
                        state.lastName.isNotBlank() ||
                        state.dni.isNotBlank() ||
                        state.email.isNotBlank() ||
                        state.phone.isNotBlank()

            } else {

                /*
                 * Editar:
                 * comparamos contra datos originales.
                 */
                state.name != originalName ||
                        state.lastName != originalLastName ||
                        state.dni != originalDni ||
                        state.email != originalEmail ||
                        state.phone != originalPhone
            }

        _uiState.value =
            state.copy(
                hasChanges =
                    hasChanges
            )
    }

    /*
     * =====================================================
     * GUARDAR
     * =====================================================
     */

    fun savePatient(
        onSuccess: () -> Unit
    ) {

        if (_uiState.value.isSaving) {
            return
        }

        if (!validate()) {
            return
        }

        val state =
            _uiState.value

        viewModelScope.launch {

            _uiState.value =
                state.copy(
                    isSaving =
                        true,

                    error =
                        null
                )

            val result =
                if (patientId == null) {

                    repository.createPatient(
                        name =
                            state.name.trim(),

                        lastName =
                            state.lastName.trim(),

                        dni =
                            state.dni,

                        email =
                            state.email.trim(),

                        phone =
                            state.phone
                    )

                } else {

                    repository.updatePatient(
                        id =
                            patientId,

                        name =
                            state.name.trim(),

                        lastName =
                            state.lastName.trim(),

                        dni =
                            state.dni,

                        email =
                            state.email.trim(),

                        phone =
                            state.phone
                    )
                }

            result
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            isSaving =
                                false,

                            /*
                             * Ya se guardó:
                             * no quedan cambios pendientes.
                             */
                            hasChanges =
                                false,

                            error =
                                null
                        )

                    if (patientId == null) {

                        UiMessageManager.success(
                            title =
                                "Paciente registrado",

                            message =
                                "El paciente fue guardado correctamente."
                        )

                    } else {

                        UiMessageManager.success(
                            title =
                                "Paciente actualizado",

                            message =
                                "Los cambios se guardaron correctamente."
                        )
                    }

                    onSuccess()
                }
                .onFailure { exception ->

                    val errorMessage =
                        exception.message
                            ?: if (patientId == null) {
                                "No se pudo registrar el paciente."
                            } else {
                                "No se pudo actualizar el paciente."
                            }

                    _uiState.value =
                        _uiState.value.copy(
                            isSaving =
                                false,

                            /*
                             * No repetimos el error inline.
                             */
                            error =
                                null
                        )

                    UiMessageManager.error(
                        title =
                            if (patientId == null) {
                                "No se pudo registrar"
                            } else {
                                "No se pudo actualizar"
                            },

                        message =
                            errorMessage
                    )
                }
        }
    }

    /*
     * =====================================================
     * VALIDACIONES
     * =====================================================
     */

    private fun validate(): Boolean {

        val state =
            _uiState.value

        val nameError =
            if (
                state.name
                    .trim()
                    .isBlank()
            ) {
                "Ingresa el nombre"
            } else {
                null
            }

        val lastNameError =
            if (
                state.lastName
                    .trim()
                    .isBlank()
            ) {
                "Ingresa el apellido"
            } else {
                null
            }

        val dniError =
            when {

                state.dni.isBlank() ->
                    "Ingresa el DNI"

                state.dni.length != 8 ->
                    "El DNI debe tener 8 dígitos"

                else ->
                    null
            }

        val emailError =
            when {

                state.email.isBlank() ->
                    "Ingresa el correo"

                !state.email.contains("@") ->
                    "Ingresa un correo válido"

                else ->
                    null
            }

        val phoneError =
            when {

                state.phone.isBlank() ->
                    "Ingresa el teléfono"

                state.phone.length != 9 ->
                    "El teléfono debe tener 9 dígitos"

                else ->
                    null
            }

        _uiState.value =
            state.copy(
                nameError =
                    nameError,

                lastNameError =
                    lastNameError,

                dniError =
                    dniError,

                emailError =
                    emailError,

                phoneError =
                    phoneError
            )

        return (
                nameError == null &&
                        lastNameError == null &&
                        dniError == null &&
                        emailError == null &&
                        phoneError == null
                )
    }
}