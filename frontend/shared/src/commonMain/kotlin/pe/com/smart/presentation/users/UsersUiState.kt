
package pe.com.smart.presentation.users

import pe.com.smart.data.remote.dto.auth.AdminUserDto
import pe.com.smart.data.remote.dto.auth.AdminUserPageDto
import pe.com.smart.data.remote.dto.auth.AvailableProfileDto
import pe.com.smart.domain.model.Speciality

enum class ProfileAvailabilityState {
    Idle,
    Loading,
    Success,
    Empty,
    Unauthorized,
    Forbidden,
    BackendUnavailable,
    Failure
}

sealed interface UserAction {
    data object Save : UserAction
    data object ResetPassword : UserAction
    data object Disable : UserAction
    data class ChangeStatus(val status: String) : UserAction
}

data class UserCreateFormState(
    val role: String = "DOCTOR",
    val profileId: Int? = null,
    val profileName: String? = null,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val lastName: String = "",
    val dni: String = "",
    val phone: String = "",
    val jobTitle: String = "",
    val specialityId: Int? = null,
    val error: String? = null,
    val isSaving: Boolean = false
)

data class UserEditFormState(
    val user: AdminUserDto,
    val username: String = user.username,
    val email: String = user.email,
    val emailVerified: Boolean = user.emailVerified,
    val roles: Set<String> = user.roles.toSet(),
    val resetPassword: String = "",
    val resetConfirmation: String = "",
    val error: String? = null,
    val isWorking: Boolean = false,
    val pendingAction: UserAction? = null
)

data class UsersUiState(
    val roles: List<String> = listOf("PATIENT", "DOCTOR", "EMPLOYEE", "ADMIN"),
    val specialities: List<Speciality> = emptyList(),
    val availableProfiles: List<AvailableProfileDto> = emptyList(),
    val profileAvailability: ProfileAvailabilityState =
        ProfileAvailabilityState.Idle,
    val profilesLoaded: Boolean = false,
    val userPage: AdminUserPageDto = AdminUserPageDto(),
    val search: String = "",
    val roleFilter: String? = null,
    val statusFilter: String? = null,
    val page: Int = 0,
    val showCreate: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val usersError: String? = null,
    val catalogError: String? = null,
    val create: UserCreateFormState = UserCreateFormState(),
    val edit: UserEditFormState? = null,
    val currentUserId: Int? = null
)

internal fun userRoleLabel(code: String) = when (code) {
    "DOCTOR" -> "Médico"
    "EMPLOYEE" -> "Empleado"
    "PATIENT" -> "Paciente"
    "ADMIN" -> "Administrador"
    else -> code
}

internal fun userStatusLabel(status: String) = when (status) {
    "PENDING" -> "Pendiente"
    "ACTIVE" -> "Activa"
    "BLOCKED" -> "Bloqueada"
    "DISABLED" -> "Desactivada"
    else -> status
}

internal val accountStatuses = listOf(
    "PENDING",
    "ACTIVE",
    "BLOCKED",
    "DISABLED"
)
