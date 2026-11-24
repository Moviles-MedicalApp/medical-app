package pe.com.smart.presentation.login

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null
) {

    val isLoginEnabled: Boolean
        get() = username.isNotBlank() &&
                password.isNotBlank() &&
                !isLoading
}