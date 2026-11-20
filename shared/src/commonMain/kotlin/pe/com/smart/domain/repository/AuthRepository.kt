package pe.com.smart.domain.repository

import pe.com.smart.data.remote.auth.dto.AdminUserRequest
import pe.com.smart.data.remote.auth.dto.AdminUserUpdateRequest
import pe.com.smart.data.remote.auth.dto.AvailableProfileDto
import pe.com.smart.data.remote.auth.dto.LoginResponse
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.data.remote.auth.dto.RegisteredUser
import pe.com.smart.data.remote.auth.dto.RoleDto
import pe.com.smart.data.remote.auth.dto.AdminUserDto
import pe.com.smart.data.remote.auth.dto.AdminUserPageDto

/** Contract for the existing authentication and user-administration operations. */
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    fun isLoggedIn(): Boolean
    fun logout()
    suspend fun refresh(): Result<LoginResponse>
    suspend fun registerPatient(request: PatientRegistrationRequest): Result<RegisteredUser>
    suspend fun createAdministrativeUser(request: AdminUserRequest): Result<RegisteredUser>
    suspend fun roles(): Result<List<RoleDto>>
    suspend fun availableProfiles(type: String): Result<List<AvailableProfileDto>>
    suspend fun users(search: String? = null, role: String? = null, status: String? = null, page: Int = 0): Result<AdminUserPageDto>
    suspend fun user(id: Int): Result<AdminUserDto>
    suspend fun updateUser(id: Int, request: AdminUserUpdateRequest): Result<AdminUserDto>
    suspend fun changeUserStatus(id: Int, status: String): Result<AdminUserDto>
    suspend fun resetUserPassword(id: Int, password: String): Result<Unit>
    suspend fun disableUser(id: Int): Result<Unit>
    suspend fun logoutRemotely()
}
