
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
) {
    /**
     * Permite que la interfaz no presente una acción de envío disponible
     * mientras falte un dato obligatorio o tenga un formato mínimo inválido.
     * El ViewModel vuelve a validar antes de realizar la solicitud.
     */
    val canSubmit: Boolean
        get() = username.trim().length in 3..64 &&
                email.trim().length <= 64 &&
                EMAIL_REGEX.matches(email.trim()) &&
                name.trim().length in 2..64 &&
                lastName.trim().length in 2..64 &&
                dni.length == 8 &&
                phone.length <= 16 &&
                password.length in 8..72 &&
                confirmation == password

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
