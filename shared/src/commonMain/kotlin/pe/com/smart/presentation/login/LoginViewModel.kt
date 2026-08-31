package pe.com.smart.presentation.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            username = value,
            usernameError = null,
            loginError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            loginError = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun login(
        onSuccess: () -> Unit
    ) {
        val currentState = _uiState.value

        val username = currentState.username.trim()
        val password = currentState.password

        var usernameError: String? = null
        var passwordError: String? = null

        if (username.isBlank()) {
            usernameError = "Ingresa tu nombre de usuario"
        }

        if (password.isBlank()) {
            passwordError = "Ingresa tu contraseña"
        }

        if (usernameError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                usernameError = usernameError,
                passwordError = passwordError
            )

            return
        }

        _uiState.value = currentState.copy(
            username = username,
            isLoading = true,
            usernameError = null,
            passwordError = null,
            loginError = null
        )

        /*
         * LOGIN TEMPORAL
         *
         * Más adelante reemplazaremos esta parte por:
         *
         * LoginUseCase
         *      ↓
         * AuthRepository
         *      ↓
         * Ktor Client
         *      ↓
         * Backend
         */

        _uiState.value = _uiState.value.copy(
            isLoading = false
        )

        onSuccess()
    }
}