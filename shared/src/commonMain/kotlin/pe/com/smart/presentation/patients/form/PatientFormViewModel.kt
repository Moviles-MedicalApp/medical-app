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

    private val repository:
            PatientRepository =
        ApiProvider.patientRepository

    private val _uiState =
        MutableStateFlow(
            PatientFormUiState()
        )

    val uiState:
            StateFlow<PatientFormUiState> =
        _uiState.asStateFlow()

    val isEditing: Boolean
        get() = patientId != null

    init {

        if (patientId != null) {
            loadPatient(patientId)
        }
    }

    private fun loadPatient(
        id: Int
    ) {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true
                )

            repository
                .getPatientById(id)
                .onSuccess { patient ->

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
                            isLoading = false
                        )
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isSaving = false
                        )

                    UiMessageManager.error(
                        exception.message
                            ?: "No se pudo guardar el paciente."
                    )
                }
        }
    }

    fun onNameChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                name = value,
                nameError = null
            )
    }

    fun onLastNameChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                lastName = value,
                lastNameError = null
            )
    }

    fun onDniChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                dni =
                    value.filter {
                        it.isDigit()
                    }.take(8),
                dniError = null
            )
    }

    fun onEmailChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                email = value,
                emailError = null
            )
    }

    fun onPhoneChange(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                phone =
                    value.filter {
                        it.isDigit()
                    }.take(9),
                phoneError = null
            )
    }

    fun savePatient(
        onSuccess: () -> Unit
    ) {

        if (!validate()) {
            return
        }

        val state =
            _uiState.value

        viewModelScope.launch {

            _uiState.value =
                state.copy(
                    isSaving = true,
                    error = null
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
                            isSaving = false
                        )

                    UiMessageManager.success(
                        if (patientId == null) {
                            "Paciente registrado correctamente."
                        } else {
                            "Paciente actualizado correctamente."
                        }
                    )

                    onSuccess()
                }
        }
    }

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
            } else null

        val lastNameError =
            if (
                state.lastName
                    .trim()
                    .isBlank()
            ) {
                "Ingresa el apellido"
            } else null

        val dniError =
            when {
                state.dni.isBlank() ->
                    "Ingresa el DNI"

                state.dni.length != 8 ->
                    "El DNI debe tener 8 dígitos"

                else -> null
            }

        val emailError =
            when {
                state.email.isBlank() ->
                    "Ingresa el correo"

                !state.email.contains("@") ->
                    "Ingresa un correo válido"

                else -> null
            }

        val phoneError =
            when {
                state.phone.isBlank() ->
                    "Ingresa el teléfono"

                state.phone.length != 9 ->
                    "El teléfono debe tener 9 dígitos"

                else -> null
            }

        _uiState.value =
            state.copy(
                nameError = nameError,
                lastNameError =
                    lastNameError,
                dniError = dniError,
                emailError =
                    emailError,
                phoneError =
                    phoneError
            )

        return nameError == null &&
                lastNameError == null &&
                dniError == null &&
                emailError == null &&
                phoneError == null
    }
}