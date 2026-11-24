
package pe.com.smart.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.auth.ApiRequestException
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.domain.usecase.AuthUseCase

class RegistrationViewModel(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrationUiState()
    )

    val uiState: StateFlow<RegistrationUiState> =
        _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update {
            it.copy(
                username = value.take(64),
                usernameError = null,
                error = null
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                error = null
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = null,
                error = null
            )
        }
    }

    fun onLastNameChange(value: String) {
        _uiState.update {
            it.copy(
                lastName = value,
                lastNameError = null,
                error = null
            )
        }
    }

    fun onDniChange(value: String) {
        _uiState.update {
            it.copy(
                dni = value.filter { char ->
                    char.isDigit()
                }.take(8),
                dniError = null,
                error = null
            )
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update {
            it.copy(
                phone = value.take(16),
                phoneError = null,
                error = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                confirmationError = null,
                error = null
            )
        }
    }

    fun onConfirmationChange(value: String) {
        _uiState.update {
            it.copy(
                confirmation = value,
                confirmationError = null,
                error = null
            )
        }
    }

    fun register(onRegistered: () -> Unit) {
        val state = _uiState.value

        if (state.isLoading) return

        if (!validate()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val request = PatientRegistrationRequest(
                    username = state.username.trim(),
                    email = state.email.trim(),
                    password = state.password,
                    name = state.name.trim(),
                    lastName = state.lastName.trim(),
                    dni = state.dni,
                    phone = state.phone.trim()
                        .ifBlank { null }
                )

                authUseCase.registerPatient(request)
                    .getOrThrow()

                // Limpiar credenciales antes
                // de ejecutar la navegación.
                _uiState.value = RegistrationUiState()

                onRegistered()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                val message = when ((exception as? ApiRequestException)?.statusCode) {
                    400 -> "Revisa los datos ingresados e inténtalo nuevamente."
                    409 -> "No se pudo completar el registro. Si ya tienes una cuenta o un perfil previo, solicita ayuda para recuperar el acceso o vincularlo de forma asistida."
                    429 -> "Se realizaron demasiados intentos. Espera un momento e inténtalo nuevamente."
                    else -> exception.message?.takeIf { it.isNotBlank() }
                        ?: "No se pudo crear la cuenta. Inténtalo nuevamente."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = message
                    )
                }
            }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value

        val usernameError = when {
            state.username.trim().length !in 3..64 ->
                "El usuario debe tener entre 3 y 64 caracteres."
            else -> null
        }

        val emailError = when {
            !Regex(
                "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
            ).matches(state.email.trim()) ||
                    state.email.trim().length > 64 ->
                "Ingresa un correo válido de hasta 64 caracteres."
            else -> null
        }

        val nameError = when {
            state.name.trim().length !in 2..64 ->
                "Ingresa nombres de entre 2 y 64 caracteres."
            else -> null
        }

        val lastNameError = when {
            state.lastName.trim().length !in 2..64 ->
                "Ingresa apellidos de entre 2 y 64 caracteres."
            else -> null
        }

        val dniError = when {
            state.dni.length != 8 ->
                "El DNI debe contener exactamente 8 dígitos."
            else -> null
        }

        val phoneError = when {
            state.phone.length > 16 ->
                "El teléfono no debe superar 16 caracteres."
            else -> null
        }

        val passwordError = when {
            state.password.length !in 8..72 ->
                "La contraseña debe tener entre 8 y 72 caracteres."
            else -> null
        }

        val confirmationError = when {
            state.confirmation != state.password ->
                "Las contraseñas no coinciden."
            else -> null
        }

        val valid = listOf(
            usernameError,
            emailError,
            nameError,
            lastNameError,
            dniError,
            phoneError,
            passwordError,
            confirmationError
        ).all { it == null }

        _uiState.update {
            it.copy(
                usernameError = usernameError,
                emailError = emailError,
                nameError = nameError,
                lastNameError = lastNameError,
                dniError = dniError,
                phoneError = phoneError,
                passwordError = passwordError,
                confirmationError = confirmationError,
                error = null
            )
        }

        return valid
    }
}
