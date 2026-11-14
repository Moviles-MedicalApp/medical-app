
package pe.com.smart.presentation.login

/**
 * Estado de la pantalla de registro.
 *
 * Las contraseñas permanecen únicamente
 * en memoria durante el formulario.
 */
data class RegistrationUiState(
    val username: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val dni: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmation: String = "",

    val isLoading: Boolean = false,
    val error: String? = null,

    val usernameError: String? = null,
    val emailError: String? = null,
    val nameError: String? = null,
    val lastNameError: String? = null,
    val dniError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmationError: String? = null
)
