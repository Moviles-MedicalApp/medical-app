package pe.com.smart.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.data.repository.AuthRepository

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository(
        ApiProvider.authApiService
    )

    private val _uiState = MutableStateFlow(
        LoginUiState()
    )

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
            isPasswordVisible =
                !_uiState.value.isPasswordVisible
        )
    }

    fun login(
        onSuccess: () -> Unit
    ) {

        val currentState = _uiState.value

        val username =
            currentState.username.trim()

        val password =
            currentState.password

        var usernameError: String? = null
        var passwordError: String? = null

        if (username.isBlank()) {
            usernameError =
                "Ingresa tu nombre de usuario"
        }

        if (password.isBlank()) {
            passwordError =
                "Ingresa tu contraseña"
        }

        if (
            usernameError != null ||
            passwordError != null
        ) {

            _uiState.value =
                currentState.copy(
                    usernameError = usernameError,
                    passwordError = passwordError
                )

            return
        }

        _uiState.value =
            currentState.copy(
                username = username,
                isLoading = true,
                usernameError = null,
                passwordError = null,
                loginError = null
            )

        viewModelScope.launch {

            repository
                .login(
                    username = username,
                    password = password
                )
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            loginError = null
                        )

                    onSuccess()
                }
                .onFailure { exception ->

                    val message = when {
                        exception.message
                            ?.contains("Failed to connect") == true -> {
                            "No se pudo conectar con el servidor"
                        }

                        exception.message
                            ?.contains("timeout", ignoreCase = true) == true -> {
                            "El servidor está tardando demasiado en responder"
                        }

                        else -> {
                            exception.message
                                ?: "Ocurrió un error al iniciar sesión"
                        }
                    }

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            loginError = message
                        )
                }
        }
    }
}