package pe.com.smart.data.repository

import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.auth.AuthApiService
import pe.com.smart.data.remote.auth.dto.LoginRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse

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

            TokenStorage.saveToken(
                response.accessToken
            )

            Result.success(response)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean =
        TokenStorage.hasToken()

    fun logout() {
        TokenStorage.clearToken()
    }
}