package pe.com.smart.presentation.doctors.form

data class DoctorFormUiState (
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val specialityId: Int? = null,
    val specialityName: String = "",

    val nameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val specialityError: String? = null,

    val hasChanges: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)