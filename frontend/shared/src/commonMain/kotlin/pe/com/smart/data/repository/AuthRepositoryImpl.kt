package pe.com.smart.data.repository

import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.auth.AuthApiService
import pe.com.smart.data.remote.auth.dto.LoginRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.data.remote.auth.dto.RegisteredUser
import pe.com.smart.data.remote.auth.dto.AdminUserRequest
import pe.com.smart.data.remote.auth.dto.*
import pe.com.smart.domain.repository.AuthRepository as AuthRepositoryContract

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepositoryContract {

    override suspend fun login(
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

    override fun isLoggedIn(): Boolean =
        TokenStorage.hasToken()

    override fun logout() {
        TokenStorage.clearTokens()
    }

    override suspend fun refresh(): Result<LoginResponse> = runCatching {
        val refreshToken = TokenStorage.getRefreshToken()
            ?: error("No hay una sesión que renovar")
        apiService.refresh(refreshToken).also {
            TokenStorage.saveTokens(it.accessToken, it.refreshToken)
        }
    }

    override suspend fun registerPatient(request: PatientRegistrationRequest): Result<RegisteredUser> =
        runCatching { apiService.registerPatient(request) }

    override suspend fun createAdministrativeUser(request: AdminUserRequest): Result<RegisteredUser> =
        runCatching { apiService.createAdminUser(request) }

    override suspend fun roles() = runCatching { apiService.getRoles() }
    override suspend fun availableProfiles(type: String) = runCatching { apiService.getAvailableProfiles(type) }
    override suspend fun users(search: String?, role: String?, status: String?, page: Int) = runCatching { apiService.getUsers(search, role, status, page) }
    override suspend fun user(id: Int) = runCatching { apiService.getUser(id) }
    override suspend fun updateUser(id: Int, request: AdminUserUpdateRequest) = runCatching { apiService.updateUser(id, request) }
    override suspend fun changeUserStatus(id: Int, status: String) = runCatching { apiService.updateUserStatus(id, status) }
    override suspend fun resetUserPassword(id: Int, password: String) = runCatching { apiService.resetPassword(id, password) }
    override suspend fun disableUser(id: Int) = runCatching { apiService.disableUser(id) }

    override suspend fun logoutRemotely() {
        val refreshToken = TokenStorage.getRefreshToken()
        try {
            if (!refreshToken.isNullOrBlank()) apiService.logout(refreshToken)
        } finally {
            TokenStorage.clearTokens()
        }
    }
}
