package pe.com.smart.domain.usecase

import pe.com.smart.data.remote.auth.dto.AdminUserRequest
import pe.com.smart.data.remote.auth.dto.AdminUserUpdateRequest
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.domain.repository.AuthRepository

/** Single entry point for the existing auth operations, following the reference app's use-case layer. */
class AuthUseCase(private val repository: AuthRepository) {
    suspend fun login(username: String, password: String) = repository.login(username, password)
    fun isLoggedIn() = repository.isLoggedIn()
    fun logout() = repository.logout()
    suspend fun refresh() = repository.refresh()
    suspend fun registerPatient(request: PatientRegistrationRequest) = repository.registerPatient(request)
    suspend fun createAdministrativeUser(request: AdminUserRequest) = repository.createAdministrativeUser(request)
    suspend fun roles() = repository.roles()
    suspend fun availableProfiles(type: String) = repository.availableProfiles(type)
    suspend fun users(search: String? = null, role: String? = null, status: String? = null, page: Int = 0) = repository.users(search, role, status, page)
    suspend fun user(id: Int) = repository.user(id)
    suspend fun updateUser(id: Int, request: AdminUserUpdateRequest) = repository.updateUser(id, request)
    suspend fun changeUserStatus(id: Int, status: String) = repository.changeUserStatus(id, status)
    suspend fun resetUserPassword(id: Int, password: String) = repository.resetUserPassword(id, password)
    suspend fun disableUser(id: Int) = repository.disableUser(id)
    suspend fun logoutRemotely() = repository.logoutRemotely()
}
