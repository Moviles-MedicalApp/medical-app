package pe.com.smart.presentation.specialities.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.repository.SpecialityRepository

class SpecialityFormViewModel(
    private val specialityId: Int?
) : ViewModel() {

    private val repository: SpecialityRepository =
        ApiProvider.specialityRepository

    private val _uiState =
        MutableStateFlow(
            SpecialityFormUiState()
        )

    val uiState: StateFlow<SpecialityFormUiState> =
        _uiState.asStateFlow()

    val isEditing: Boolean
        get() = specialityId != null

    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalDeleted: Boolean = false

    init {
        if (specialityId != null) {
            loadSpeciality(specialityId)
        }
    }

    private fun loadSpeciality(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository
                .getSpecialityById(id)
                .onSuccess { speciality ->
                    originalName = speciality.name
                    originalDescription = speciality.description
                    originalDeleted = speciality.deleted

                    _uiState.value = _uiState.value.copy(
                        name = speciality.name,
                        description = speciality.description,
                        deleted = speciality.deleted,
                        hasChanges = false,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    val errorMessage = exception.message
                        ?: "No se pudo cargar la especialidad."

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )

                    UiMessageManager.error(
                        title = "No se pudo cargar",
                        message = errorMessage
                    )
                }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value,
            nameError = null
        )
        updateHasChanges()
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value,
            descriptionError = null
        )
        updateHasChanges()
    }

    fun onDeletedChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            deleted = value
        )
        updateHasChanges()
    }

    private fun updateHasChanges() {
        val state = _uiState.value
        val hasChanges = if (specialityId == null) {
            state.name.isNotBlank() || state.description.isNotBlank()
        } else {
            state.name != originalName ||
                    state.description != originalDescription ||
                    state.deleted != originalDeleted
        }

        _uiState.value = state.copy(hasChanges = hasChanges)
    }

    fun saveSpeciality(onSuccess: () -> Unit) {
        if (_uiState.value.isSaving) return
        if (!validate()) return

        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)

            val result = if (specialityId == null) {
                repository.createSpeciality(
                    name = state.name.trim(),
                    description = state.description.trim(),
                    deleted = state.deleted
                )
            } else {
                repository.updateSpeciality(
                    id = specialityId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    deleted = state.deleted
                )
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        hasChanges = false,
                        error = null
                    )

                    if (specialityId == null) {
                        UiMessageManager.success(
                            title = "Especialidad registrada",
                            message = "La especialidad fue guardada correctamente."
                        )
                    } else {
                        UiMessageManager.success(
                            title = "Especialidad actualizada",
                            message = "Los cambios se guardaron correctamente."
                        )
                    }

                    onSuccess()
                }
                .onFailure { exception ->
                    val errorMessage = exception.message
                        ?: if (specialityId == null) {
                            "No se pudo registrar la especialidad."
                        } else {
                            "No se pudo actualizar la especialidad."
                        }

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = null
                    )

                    UiMessageManager.error(
                        title = if (specialityId == null) "No se pudo registrar" else "No se pudo actualizar",
                        message = errorMessage
                    )
                }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value

        val nameError = if (state.name.trim().isBlank()) {
            "Ingresa el nombre de la especialidad"
        } else {
            null
        }

        _uiState.value = state.copy(
            nameError = nameError
        )

        return nameError == null
    }
}
