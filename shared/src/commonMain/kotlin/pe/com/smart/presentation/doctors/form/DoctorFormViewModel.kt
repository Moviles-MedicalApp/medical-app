package pe.com.smart.presentation.doctors.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Speciality
import pe.com.smart.domain.repository.DoctorRepository
import pe.com.smart.domain.repository.SpecialityRepository

class DoctorFormViewModel(
    private val doctorId: Int?
) : ViewModel() {

    private val doctorRepository: DoctorRepository =
        ApiProvider.doctorRepository

    private val specialityRepository: SpecialityRepository =
        ApiProvider.specialityRepository

    private val _uiState = MutableStateFlow(DoctorFormUiState())
    val uiState: StateFlow<DoctorFormUiState> = _uiState.asStateFlow()

    private val _specialities = MutableStateFlow<List<Speciality>>(emptyList())
    val specialities: StateFlow<List<Speciality>> = _specialities.asStateFlow()

    val isEditing: Boolean
        get() = doctorId != null

    private var originalName = ""
    private var originalLastName = ""
    private var originalEmail = ""
    private var originalPhone = ""
    private var originalSpecialityId: Int? = null

    init {
        loadSpecialities()

        if (doctorId != null) {
            loadDoctor(doctorId)
        }
    }

    private fun loadSpecialities() {
        viewModelScope.launch {
            specialityRepository.getSpecialities()
                .onSuccess { list ->
                    _specialities.value = list
                }
                .onFailure { exception ->
                    val message = exception.message
                        ?: "No se pudieron cargar las especialidades."

                    _uiState.value = _uiState.value.copy(error = message)

                    UiMessageManager.error(
                        title = "Error al cargar especialidades",
                        message = message
                    )
                }
        }
    }

    private fun loadDoctor(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            doctorRepository.getDoctorById(id)
                .onSuccess { doctor ->
                    originalName = doctor.name
                    originalLastName = doctor.lastName
                    originalEmail = doctor.email.orEmpty()
                    originalPhone = doctor.phone.orEmpty()
                    originalSpecialityId = doctor.specialityId

                    _uiState.value = _uiState.value.copy(
                        name = doctor.name,
                        lastName = doctor.lastName,
                        email = doctor.email.orEmpty(),
                        phone = doctor.phone.orEmpty(),
                        specialityId = doctor.specialityId,
                        specialityName = doctor.specialityName,
                        hasChanges = false,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    val message = exception.message
                        ?: "No se pudo cargar el médico."

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message
                    )

                    UiMessageManager.error(
                        title = "No se pudo cargar",
                        message = message
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

    fun onLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            lastName = value,
            lastNameError = null
        )
        updateHasChanges()
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null
        )
        updateHasChanges()
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(
            phone = value.filter(Char::isDigit).take(9),
            phoneError = null
        )
        updateHasChanges()
    }

    fun onSpecialityChange(speciality: Speciality) {
        _uiState.value = _uiState.value.copy(
            specialityId = speciality.id,
            specialityName = speciality.name,
            specialityError = null
        )
        updateHasChanges()
    }

    private fun updateHasChanges() {
        val state = _uiState.value

        val changed = if (doctorId == null) {
            state.name.isNotBlank() ||
                    state.lastName.isNotBlank() ||
                    state.email.isNotBlank() ||
                    state.phone.isNotBlank() ||
                    state.specialityId != null
        } else {
            state.name != originalName ||
                    state.lastName != originalLastName ||
                    state.email != originalEmail ||
                    state.phone != originalPhone ||
                    state.specialityId != originalSpecialityId
        }

        _uiState.value = state.copy(hasChanges = changed)
    }

    fun saveDoctor(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.isSaving || !validate()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null
            )

            val result = if (doctorId == null) {
                doctorRepository.createDoctor(
                    name = state.name.trim(),
                    lastName = state.lastName.trim(),
                    email = state.email.trim(),
                    phone = state.phone,
                    specialityId = state.specialityId!!
                )
            } else {
                doctorRepository.updateDoctor(
                    id = doctorId,
                    name = state.name.trim(),
                    lastName = state.lastName.trim(),
                    email = state.email.trim(),
                    phone = state.phone,
                    specialityId = state.specialityId!!
                )
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        hasChanges = false
                    )

                    UiMessageManager.success(
                        title = if (doctorId == null) {
                            "Médico registrado"
                        } else {
                            "Médico actualizado"
                        },
                        message = if (doctorId == null) {
                            "El médico se registró correctamente."
                        } else {
                            "Los cambios se guardaron correctamente."
                        }
                    )

                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(isSaving = false)

                    UiMessageManager.error(
                        title = if (doctorId == null) {
                            "No se pudo registrar"
                        } else {
                            "No se pudo actualizar"
                        },
                        message = exception.message
                            ?: "Ocurrió un error al guardar el médico."
                    )
                }
        }
    }

    private fun validate(): Boolean {
        val state = _uiState.value

        val nameError = if (state.name.isBlank()) {
            "Ingresa el nombre."
        } else null

        val lastNameError = if (state.lastName.isBlank()) {
            "Ingresa los apellidos."
        } else null

        val emailError = when {
            state.email.isBlank() ->
                "Ingresa el correo."

            !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                .matches(state.email.trim()) ->
                "Ingresa un correo válido."

            else -> null
        }

        val phoneError = when {
            state.phone.isBlank() -> "Ingresa el teléfono."
            state.phone.length != 9 -> "El teléfono debe tener 9 dígitos."
            else -> null
        }

        val specialityError = if (state.specialityId == null) {
            "Selecciona una especialidad."
        } else null

        _uiState.value = state.copy(
            nameError = nameError,
            lastNameError = lastNameError,
            emailError = emailError,
            phoneError = phoneError,
            specialityError = specialityError
        )

        return nameError == null &&
                lastNameError == null &&
                emailError == null &&
                phoneError == null &&
                specialityError == null
    }
}
