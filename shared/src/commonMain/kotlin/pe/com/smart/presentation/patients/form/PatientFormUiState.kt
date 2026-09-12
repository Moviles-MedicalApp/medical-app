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

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)