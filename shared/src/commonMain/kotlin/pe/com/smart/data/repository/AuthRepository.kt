package pe.com.smart.data.repository

import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.auth.AuthApiService
import pe.com.smart.data.remote.auth.dto.LoginRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.data.remote.auth.dto.RegisteredUser
import pe.com.smart.data.remote.auth.dto.AdminUserRequest
import pe.com.smart.data.remote.auth.dto.*

class AuthRepository(
    private val apiService: AuthApiService
) {

    suspend fun login(
        username: String,
        password: String
    ): Result<LoginResponse> {

        return try {

            val response =
                apiService.login(
                    LoginRequest(
                        username = username,
                        password = password
                    )
                )

            TokenStorage.saveTokens(response.accessToken, response.refreshToken)

            Result.success(response)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean =
        TokenStorage.hasToken()

    fun logout() {
        TokenStorage.clearTokens()
    }

    suspend fun refresh(): Result<LoginResponse> = runCatching {
        val refreshToken = TokenStorage.getRefreshToken()
            ?: error("No hay una sesión que renovar")
        apiService.refresh(refreshToken).also {
            TokenStorage.saveTokens(it.accessToken, it.refreshToken)
        }
    }

    suspend fun registerPatient(request: PatientRegistrationRequest): Result<RegisteredUser> =
        runCatching { apiService.registerPatient(request) }

    suspend fun createAdministrativeUser(request: AdminUserRequest): Result<RegisteredUser> =
        runCatching { apiService.createAdminUser(request) }

    suspend fun roles() = runCatching { apiService.getRoles() }
    suspend fun availableProfiles(type: String) = runCatching { apiService.getAvailableProfiles(type) }
    suspend fun users(search: String? = null, role: String? = null, status: String? = null, page: Int = 0) = runCatching { apiService.getUsers(search, role, status, page) }
    suspend fun user(id: Int) = runCatching { apiService.getUser(id) }
    suspend fun updateUser(id: Int, request: AdminUserUpdateRequest) = runCatching { apiService.updateUser(id, request) }
    suspend fun changeUserStatus(id: Int, status: String) = runCatching { apiService.updateUserStatus(id, status) }
    suspend fun resetUserPassword(id: Int, password: String) = runCatching { apiService.resetPassword(id, password) }
    suspend fun disableUser(id: Int) = runCatching { apiService.disableUser(id) }

    suspend fun logoutRemotely() {
        val refreshToken = TokenStorage.getRefreshToken()
        try {
            if (!refreshToken.isNullOrBlank()) apiService.logout(refreshToken)
        } finally {
            TokenStorage.clearTokens()
        }
    }
}
