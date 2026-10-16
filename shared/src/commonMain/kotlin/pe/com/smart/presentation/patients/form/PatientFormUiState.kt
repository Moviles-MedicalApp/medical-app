package pe.com.smart.presentation.patients.form

data class PatientFormUiState(
    val name: String = "",
    val lastName: String = "",
    val dni: String = "",
    val email: String = "",
    val phone: String = "",

    val nameError: String? = null,
    val lastNameError: String? = null,
    val dniError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,

    /*
     * Indica si el usuario modificó
     * algún dato del formulario.
     */
    val hasChanges: Boolean = false,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    /*
     * Se reserva principalmente para
     * errores que impidan cargar el formulario.
     *
     * Los errores de guardado se muestran
     * mediante UiMessageManager.
     */
    val error: String? = null
)