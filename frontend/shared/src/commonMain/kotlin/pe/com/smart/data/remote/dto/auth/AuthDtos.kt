package pe.com.smart.data.remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class PatientRegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val name: String,
    val lastName: String,
    val dni: String,
    val phone: String? = null
)

@Serializable
data class RegisteredUser(
    val id: Int,
    val patientId: Int? = null,
    val profileId: Int? = null,
    val username: String,
    val email: String,
    val roles: List<String> = emptyList()
)

/** A transport model used only by the administrator endpoint. */
@Serializable
data class AdminUserRequest(
    val role: String,
    val profileId: Int? = null,
    val username: String,
    val email: String,
    val password: String,
    val name: String? = null,
    val lastName: String? = null,
    val dni: String? = null,
    val phone: String? = null,
    val jobTitle: String? = null,
    val specialityId: Int? = null
)

@Serializable data class RoleDto(val id: Int, val code: String, val name: String, val description: String? = null)
@Serializable data class AvailableProfileDto(val type: String, val profileId: Int, val fullName: String, val identifier: String)
@Serializable data class UserProfileReferenceDto(val type: String, val profileId: Int, val fullName: String, val deleted: Boolean = false)
@Serializable data class AdminUserDto(val id: Int, val username: String, val email: String, val status: String, val emailVerified: Boolean = false, val roles: List<String> = emptyList(), val profiles: List<UserProfileReferenceDto> = emptyList(), val createdAt: String? = null, val updatedAt: String? = null)
@Serializable data class AdminUserPageDto(val content: List<AdminUserDto> = emptyList(), val page: Int = 0, val size: Int = 20, val totalElements: Long = 0, val totalPages: Int = 0)
@Serializable data class AdminUserUpdateRequest(val username: String? = null, val email: String? = null, val emailVerified: Boolean? = null, val roles: List<String>? = null)
@Serializable data class UserStatusRequest(val status: String)
@Serializable data class PasswordResetRequest(val temporaryPassword: String)
