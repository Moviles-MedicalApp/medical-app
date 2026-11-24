
package pe.com.smart.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.api.auth.ApiRequestException
import pe.com.smart.data.remote.dto.auth.AdminUserDto
import pe.com.smart.data.remote.dto.auth.AdminUserRequest
import pe.com.smart.data.remote.dto.auth.AdminUserUpdateRequest
import pe.com.smart.domain.usecase.AuthUseCase
import pe.com.smart.domain.usecase.SpecialityUseCase

class UsersViewModel(
    private val authUseCase: AuthUseCase,
    private val specialityUseCase: SpecialityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UsersUiState(
            currentUserId = AccessTokenClaimsDecoder
                .decode(TokenStorage.getToken().orEmpty())
                ?.userId
        )
    )

    val uiState: StateFlow<UsersUiState> =
        _uiState.asStateFlow()

    private var usersJob: Job? = null
    private var profilesJob: Job? = null
    private var catalogsJob: Job? = null

    init {
        loadCatalogs()
        loadUsers()
        refreshAvailableProfiles()
    }

    // =====================================================
    // CATÁLOGOS
    // =====================================================

    fun loadCatalogs() {
        catalogsJob?.cancel()

        catalogsJob = viewModelScope.launch {
            try {
                val rolesResult = authUseCase.roles()
                val specialityResult =
                    specialityUseCase
                        .getSpecialities()

                _uiState.update { state ->
                    state.copy(
                        roles = rolesResult.getOrNull()
                            ?.map { role -> role.code }
                            ?: state.roles,
                        specialities = specialityResult
                            .getOrNull()
                            ?.filterNot { speciality ->
                                speciality.deleted
                            }
                            ?: state.specialities,
                        catalogError =
                            rolesResult.exceptionOrNull()?.message
                                ?: specialityResult
                                    .exceptionOrNull()?.message
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        catalogError = exception.message
                            ?: "No se pudieron cargar los catálogos."
                    )
                }
            }
        }
    }

    // =====================================================
    // NAVEGACIÓN INTERNA
    // =====================================================

    fun setCreateVisible(visible: Boolean) {
        _uiState.update { state ->
            state.copy(
                showCreate = visible,
                create = state.create.copy(error = null)
            )
        }
    }

    fun applyProvisioningSelection() {
        val profileId =
            UserProvisioningSelection.profileId ?: return

        val role =
            UserProvisioningSelection.role ?: "DOCTOR"

        _uiState.update { state ->
            state.copy(
                showCreate = true,
                create = UserCreateFormState(
                    role = role,
                    profileId = profileId,
                    profileName =
                        UserProvisioningSelection.profileName,
                    specialityId =
                        UserProvisioningSelection.specialityId,
                    email =
                        UserProvisioningSelection.email.orEmpty()
                )
            )
        }

        refreshAvailableProfiles()
    }

    // =====================================================
    // LISTADO DE USUARIOS
    // =====================================================

    fun onSearchChange(value: String) {
        _uiState.update { state ->
            state.copy(search = value)
        }
    }

    fun onRoleFilter(value: String?) {
        _uiState.update { state ->
            state.copy(
                roleFilter = value,
                page = 0
            )
        }
        loadUsers()
    }

    fun onStatusFilter(value: String?) {
        _uiState.update { state ->
            state.copy(
                statusFilter = value,
                page = 0
            )
        }
        loadUsers()
    }

    fun searchUsers() {
        _uiState.update { state ->
            state.copy(page = 0)
        }
        loadUsers()
    }

    fun changePage(page: Int) {
        _uiState.update { state ->
            state.copy(
                page = page.coerceAtLeast(0)
            )
        }
        loadUsers()
    }

    fun loadUsers() {
        usersJob?.cancel()

        usersJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoadingUsers = true,
                    usersError = null
                )
            }

            try {
                val current = _uiState.value

                val result = authUseCase.users(
                    search = current.search
                        .trim()
                        .ifBlank { null },
                    role = current.roleFilter,
                    status = current.statusFilter,
                    page = current.page
                )

                val response = result.getOrThrow()

                _uiState.update { state ->
                    state.copy(
                        userPage = response,
                        isLoadingUsers = false,
                        usersError = null
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoadingUsers = false,
                        usersError = exception.message
                            ?: "No se pudieron cargar los usuarios."
                    )
                }
            }
        }
    }

    // =====================================================
    // CREACIÓN DE USUARIOS
    // =====================================================

    fun changeCreateRole(role: String) {
        _uiState.update { state ->
            state.copy(
                create = UserCreateFormState(role = role)
            )
        }

        refreshAvailableProfiles()
    }

    fun updateCreate(
        transform: (UserCreateFormState) -> UserCreateFormState
    ) {
        _uiState.update { state ->
            state.copy(
                create = transform(state.create)
                    .copy(error = null)
            )
        }
    }

    fun selectProfile(profileId: Int?) {
        val current = _uiState.value

        val profile = current.availableProfiles
            .firstOrNull { item ->
                item.profileId == profileId
            }

        updateCreate { form ->
            form.copy(
                profileId = profileId,
                profileName = profile?.fullName,
                email = when {
                    profileId == null -> ""

                    form.role == "DOCTOR" ->
                        profile?.identifier.orEmpty()

                    else -> form.email
                },
                specialityId =
                    if (profileId == null) {
                        null
                    } else {
                        form.specialityId
                    }
            )
        }
    }

    // =====================================================
    // PERFILES DISPONIBLES
    // =====================================================

    fun refreshAvailableProfiles() {
        profilesJob?.cancel()

        val selectedRole = _uiState.value.create.role

        if (selectedRole == "ADMIN") {
            _uiState.update { state ->
                state.copy(
                    availableProfiles = emptyList(),
                    profilesLoaded = true,
                    profileAvailability =
                        ProfileAvailabilityState.Idle
                )
            }
            return
        }

        profilesJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    profilesLoaded = false,
                    availableProfiles = emptyList(),
                    profileAvailability =
                        ProfileAvailabilityState.Loading
                )
            }

            try {
                val result = authUseCase.availableProfiles(
                    selectedRole
                )

                val profiles = result.getOrThrow()

                _uiState.update { state ->
                    state.copy(
                        availableProfiles = profiles,
                        profilesLoaded = true,
                        profileAvailability =
                            if (profiles.isEmpty()) {
                                ProfileAvailabilityState.Empty
                            } else {
                                ProfileAvailabilityState.Success
                            }
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                val availability = when (
                    (exception as? ApiRequestException)?.statusCode
                ) {
                    401 -> ProfileAvailabilityState.Unauthorized
                    403 -> ProfileAvailabilityState.Forbidden
                    404 -> ProfileAvailabilityState.BackendUnavailable
                    else -> ProfileAvailabilityState.Failure
                }

                _uiState.update { state ->
                    state.copy(
                        profilesLoaded = true,
                        profileAvailability = availability
                    )
                }
            }
        }
    }

    // =====================================================
    // GUARDAR NUEVO USUARIO
    // =====================================================

    fun createUser() {
        val current = _uiState.value
        val form = current.create

        if (form.isSaving) return

        val validationError: String? = validateCreate(form)

        if (validationError != null) {
            _uiState.update { state ->
                state.copy(
                    create = state.create.copy(
                        error = validationError
                    )
                )
            }
            return
        }

        if (form.profileId != null) {
            val isAvailable =
                current.profilesLoaded &&
                        current.profileAvailability ==
                        ProfileAvailabilityState.Success &&
                        current.availableProfiles.any { profile ->
                            profile.profileId == form.profileId
                        }

            if (!isAvailable) {
                _uiState.update { state ->
                    state.copy(
                        create = state.create.copy(
                            error = "No se pudo verificar la disponibilidad del perfil seleccionado."
                        )
                    )
                }
                return
            }
        }

        _uiState.update { state ->
            state.copy(
                create = state.create.copy(
                    isSaving = true,
                    error = null
                )
            )
        }

        viewModelScope.launch {
            try {
                val isNewProfile = form.profileId == null

                val request = AdminUserRequest(
                    role = form.role,
                    profileId = form.profileId,
                    username = form.username.trim(),
                    email = form.email.trim(),
                    password = form.password,
                    name = form.name.trim().takeIf {
                        isNewProfile && it.isNotBlank()
                    },
                    lastName = form.lastName.trim().takeIf {
                        isNewProfile && it.isNotBlank()
                    },
                    dni = form.dni.takeIf {
                        isNewProfile && it.isNotBlank()
                    },
                    phone = form.phone.takeIf {
                        isNewProfile && it.isNotBlank()
                    },
                    jobTitle = form.jobTitle.takeIf {
                        isNewProfile && it.isNotBlank()
                    },
                    specialityId = form.specialityId.takeIf {
                        isNewProfile
                    }
                )

                val created = authUseCase
                    .createAdministrativeUser(request)
                    .getOrThrow()

                UiMessageManager.success(
                    title = "Usuario creado",
                    message = "La cuenta ${created.username} quedó habilitada."
                )

                UserProvisioningSelection.clear()

                _uiState.update { state ->
                    state.copy(
                        create = UserCreateFormState(role = form.role),
                        showCreate = false,
                        page = 0
                    )
                }

                loadUsers()
                refreshAvailableProfiles()

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        create = state.create.copy(
                            isSaving = false,
                            error = exception.message
                                ?: "No se pudo crear el usuario."
                        )
                    )
                }
            }
        }
    }

    // =====================================================
    // EDICIÓN DE USUARIOS
    // =====================================================

    fun openUser(user: AdminUserDto) {
        _uiState.update { state ->
            state.copy(
                edit = UserEditFormState(user)
            )
        }
    }

    fun closeUser() {
        if (_uiState.value.edit?.isWorking == true) {
            return
        }

        _uiState.update { state ->
            state.copy(edit = null)
        }
    }

    fun updateEdit(
        transform: (UserEditFormState) -> UserEditFormState
    ) {
        _uiState.update { state ->
            val currentEdit = state.edit

            if (currentEdit == null) {
                state
            } else {
                state.copy(
                    edit = transform(currentEdit)
                        .copy(error = null)
                )
            }
        }
    }

    fun toggleEditRole(role: String) {
        updateEdit { form ->
            val isOwnAdmin =
                form.user.id == _uiState.value.currentUserId &&
                        role == "ADMIN"

            val canAssign =
                role == "ADMIN" ||
                        form.user.profiles.any { profile ->
                            profile.type == role &&
                                    !profile.deleted
                        }

            if (isOwnAdmin || !canAssign) {
                form
            } else {
                val updatedRoles =
                    if (role in form.roles) {
                        form.roles - role
                    } else {
                        form.roles + role
                    }

                form.copy(roles = updatedRoles)
            }
        }
    }

    // =====================================================
    // ACCIONES ADMINISTRATIVAS
    // =====================================================

    fun requestAction(action: UserAction) {
        _uiState.update { state ->
            val edit = state.edit

            if (edit == null || edit.isWorking) {
                state
            } else {
                state.copy(
                    edit = edit.copy(
                        pendingAction = action,
                        error = null
                    )
                )
            }
        }
    }

    fun cancelAction() {
        updateEdit { form ->
            if (form.isWorking) {
                form
            } else {
                form.copy(
                    pendingAction = null
                )
            }
        }
    }

    fun confirmAction() {
        val edit = _uiState.value.edit ?: return
        val action = edit.pendingAction ?: return

        if (edit.isWorking) return

        val validationError: String? =
            validateAction(edit, action)

        if (validationError != null) {
            _uiState.update { state ->
                state.copy(
                    edit = edit.copy(
                        error = validationError,
                        pendingAction = null
                    )
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                edit = edit.copy(
                    isWorking = true,
                    error = null
                )
            )
        }

        viewModelScope.launch {
            try {
                when (action) {

                    UserAction.Save -> {
                        val request = AdminUserUpdateRequest(
                            username = edit.username.trim(),
                            email = edit.email.trim(),
                            emailVerified = edit.emailVerified,
                            roles = edit.roles.toList()
                        )

                        val updated = authUseCase
                            .updateUser(
                                edit.user.id,
                                request
                            )
                            .getOrThrow()

                        _uiState.update { state ->
                            state.copy(
                                edit = UserEditFormState(updated)
                            )
                        }

                        UiMessageManager.success(
                            title = "Cuenta actualizada",
                            message = "Los cambios se guardaron correctamente."
                        )

                        loadUsers()
                    }

                    is UserAction.ChangeStatus -> {
                        authUseCase.changeUserStatus(
                            edit.user.id,
                            action.status
                        ).getOrThrow()

                        refreshSelectedUser(edit.user.id)

                        UiMessageManager.success(
                            title = "Estado actualizado",
                            message = "La cuenta ahora está ${
                                userStatusLabel(
                                    action.status
                                ).lowercase()
                            }."
                        )

                        loadUsers()
                    }

                    UserAction.Disable -> {
                        authUseCase.disableUser(
                            edit.user.id
                        ).getOrThrow()

                        refreshSelectedUser(edit.user.id)

                        UiMessageManager.success(
                            title = "Cuenta desactivada",
                            message = "La cuenta se desactivó sin borrar su historial."
                        )

                        loadUsers()
                    }

                    UserAction.ResetPassword -> {
                        authUseCase.resetUserPassword(
                            edit.user.id,
                            edit.resetPassword
                        ).getOrThrow()

                        _uiState.update { state ->
                            state.copy(
                                edit = state.edit?.copy(
                                    resetPassword = "",
                                    resetConfirmation = ""
                                )
                            )
                        }

                        UiMessageManager.success(
                            title = "Contraseña restablecida",
                            message = "Comparte la clave temporal por un canal seguro."
                        )
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        edit = state.edit?.copy(
                            isWorking = false,
                            pendingAction = null
                        )
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        edit = state.edit?.copy(
                            isWorking = false,
                            pendingAction = null,
                            error = exception.message
                                ?: "No se pudo completar la operación."
                        )
                    )
                }
            }
        }
    }

    // =====================================================
    // RECARGAR USUARIO SELECCIONADO
    // =====================================================

    private suspend fun refreshSelectedUser(id: Int) {
        val result = authUseCase.user(id)

        val updated = result.getOrThrow()

        _uiState.update { state ->
            state.copy(
                edit = UserEditFormState(
                    user = updated,
                    isWorking = true
                )
            )
        }
    }

    // =====================================================
    // VALIDACIÓN DE ACCIONES
    // =====================================================

    private fun validateAction(
        edit: UserEditFormState,
        action: UserAction
    ): String? {
        val ownAccount =
            _uiState.value.currentUserId == edit.user.id

        return when (action) {

            UserAction.Save -> {
                when {
                    edit.username.trim().length !in 3..64 ->
                        "El usuario debe tener entre 3 y 64 caracteres."

                    !isValidEmail(edit.email) ->
                        "Ingresa un correo válido."

                    edit.roles.isEmpty() ->
                        "La cuenta debe conservar al menos un rol."

                    ownAccount && "ADMIN" !in edit.roles ->
                        "No puedes retirar tu propio rol de administrador."

                    edit.roles.any { role ->
                        role != "ADMIN" &&
                                edit.user.profiles.none { profile ->
                                    profile.type == role &&
                                            !profile.deleted
                                }
                    } ->
                        "Los roles clínicos u operativos requieren un perfil activo vinculado."

                    else -> null
                }
            }

            UserAction.ResetPassword -> {
                when {
                    edit.resetPassword.length !in 8..72 ->
                        "La contraseña debe tener entre 8 y 72 caracteres."

                    edit.resetPassword != edit.resetConfirmation ->
                        "Las contraseñas no coinciden."

                    else -> null
                }
            }

            UserAction.Disable -> {
                if (ownAccount) {
                    "No puedes desactivar tu propia cuenta."
                } else {
                    null
                }
            }

            is UserAction.ChangeStatus -> {
                when {
                    ownAccount ->
                        "No puedes cambiar el estado de tu propia cuenta."

                    edit.user.status == "DISABLED" ->
                        "Una cuenta desactivada no puede reactivarse desde esta operación."

                    else -> null
                }
            }
        }
    }

    // =====================================================
    // VALIDACIÓN DE NUEVA CUENTA
    // =====================================================

    private fun validateCreate(
        form: UserCreateFormState
    ): String? {
        return when {
            form.username.trim().length !in 3..64 ->
                "El usuario debe tener entre 3 y 64 caracteres."

            !isValidEmail(form.email) ||
                    form.email.length > 64 ->
                "Ingresa un correo válido."

            form.password.length !in 8..72 ->
                "La contraseña temporal debe tener entre 8 y 72 caracteres."

            form.password != form.confirmPassword ->
                "Las contraseñas no coinciden."

            form.profileId == null &&
                    form.role !in setOf("ADMIN", "PATIENT") &&
                    (
                            form.name.trim().length !in 2..64 ||
                                    form.lastName.trim().length !in 2..64
                            ) ->
                "Ingresa nombres y apellidos válidos."

            form.role == "PATIENT" &&
                    form.profileId == null ->
                    "Para crear acceso a un paciente, selecciona su perfil clínico existente. No se creará un segundo perfil."

            form.role == "EMPLOYEE" &&
                    form.profileId == null &&
                    form.dni.length != 8 ->
                "El DNI del empleado debe tener 8 dígitos."

            form.role == "DOCTOR" &&
                    form.profileId == null &&
                    form.specialityId == null ->
                "Selecciona una especialidad."

            else -> null
        }
    }

    // =====================================================
    // VALIDAR CORREO
    // =====================================================

    private fun isValidEmail(value: String): Boolean {
        val email = value.trim()

        return Regex(
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"
        ).matches(email)
    }
}
