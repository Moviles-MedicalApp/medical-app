package pe.com.smart.presentation.specialities.form

data class SpecialityFormUiState(
    val name: String = "",
    val description: String = "",
    val active: Boolean = true,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val error: String? = null
)
