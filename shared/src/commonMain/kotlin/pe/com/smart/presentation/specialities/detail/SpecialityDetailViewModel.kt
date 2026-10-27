package pe.com.smart.presentation.specialities.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.repository.SpecialityRepository

class SpecialityDetailViewModel(
    private val specialityId: Int
) : ViewModel() {

    private val repository: SpecialityRepository =
        ApiProvider.specialityRepository

    private val _uiState =
        MutableStateFlow(
            SpecialityDetailUiState()
        )

    val uiState: StateFlow<SpecialityDetailUiState> =
        _uiState.asStateFlow()

    fun loadSpeciality() {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

            repository
                .getSpecialityById(specialityId)
                .onSuccess { speciality ->
                    /*
                     * Médicos de prueba o asociados a la especialidad
                     */
                    val mockDoctors = listOf(
                        Doctor(
                            id = 1,
                            name = "Carlos",
                            lastName = "Gómez",
                            fullName = "Dr. Carlos Gómez",
                            email = "carlos.gomez@clinica.com",
                            phone = "987654321",
                            specialityId = speciality.id,
                            specialityName = speciality.name

                        ),
                        Doctor(
                            id = 2,
                            name = "María",
                            lastName = "Torres",
                            fullName = "Dra. María Torres",
                            email = "maria.torres@clinica.com",
                            phone = "912345678",
                            specialityId = speciality.id,
                            specialityName = speciality.name
                        )
                    )

                    _uiState.value =
                        _uiState.value.copy(
                            speciality = speciality,
                            doctors = mockDoctors,
                            isLoading = false,
                            error = null
                        )
                }
                .onFailure { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            error = exception.message
                                ?: "No se pudo cargar la especialidad."
                        )
                }
        }
    }

    fun deleteSpeciality(
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isDeleting) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isDeleting = true,
                    error = null
                )

            repository
                .deleteSpeciality(specialityId)
                .onSuccess {
                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting = false
                        )

                    UiMessageManager.success(
                        title = "Especialidad eliminada",
                        message = "La especialidad fue eliminada correctamente."
                    )

                    onSuccess()
                }
                .onFailure { exception ->
                    val errorMessage = exception.message
                        ?: "No se pudo eliminar la especialidad."

                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting = false,
                            error = errorMessage
                        )

                    UiMessageManager.error(
                        title = "No se pudo eliminar",
                        message = errorMessage
                    )
                }
        }
    }
}
