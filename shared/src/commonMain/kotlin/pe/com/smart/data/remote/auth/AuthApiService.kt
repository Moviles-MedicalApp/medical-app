package pe.com.smart.data.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import pe.com.smart.core.network.ApiConstants
import pe.com.smart.data.remote.auth.dto.LoginRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse
import pe.com.smart.data.remote.auth.dto.RefreshRequest
import pe.com.smart.data.remote.auth.dto.PatientRegistrationRequest
import pe.com.smart.data.remote.auth.dto.RegisteredUser
import pe.com.smart.data.remote.auth.dto.AdminUserRequest
import pe.com.smart.data.remote.auth.dto.*

class ApiRequestException(
    val statusCode: Int,
    message: String
) : Exception(message)

private fun apiError(status: HttpStatusCode, fallback: String): ApiRequestException {
    val message = when (status) {
        HttpStatusCode.BadRequest -> "Revisa los datos ingresados e inténtalo nuevamente."
        HttpStatusCode.Unauthorized -> "Tu sesión venció. Inicia sesión nuevamente."
        HttpStatusCode.Forbidden -> "No tienes permisos para realizar esta operación."
        HttpStatusCode.NotFound -> "No se encontró el perfil solicitado. Actualiza la lista e inténtalo nuevamente."
        HttpStatusCode.Conflict -> "No se pudo completar la operación porque los datos ya existen o el perfil dejó de estar disponible. Verifica los datos o selecciona otro perfil."
        HttpStatusCode.TooManyRequests -> "Se realizaron demasiados intentos. Espera un momento e inténtalo nuevamente."
        else -> fallback
    }
    return ApiRequestException(status.value, message)
}

class AuthApiService(
    private val client: HttpClient
) {

    suspend fun login(
        request: LoginRequest
    ): LoginResponse {

        val response = client.post(
            "${ApiConstants.AUTH_SERVER}/auth/login"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        return when (response.status) {

            HttpStatusCode.OK -> {
                response.body<LoginResponse>()
            }

            HttpStatusCode.Unauthorized -> {
                throw Exception(
                    "Usuario o contraseña incorrectos"
                )
            }

            HttpStatusCode.BadRequest -> {
                throw Exception(
                    "Datos de acceso inválidos"
                )
            }

            HttpStatusCode.Forbidden -> {
                throw Exception(
                    "No tienes permiso para acceder"
                )
            }

            HttpStatusCode.InternalServerError -> {
                throw Exception(
                    "Error interno del servidor"
                )
            }

            else -> {
                throw Exception(
                    "Error al iniciar sesión (${response.status.value})"
                )
            }
        }
    }

    suspend fun refresh(refreshToken: String): LoginResponse {
        val response = client.post("${ApiConstants.AUTH_SERVER}/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("No se pudo renovar la sesión")
        }
        return response.body()
    }

    suspend fun registerPatient(request: PatientRegistrationRequest): RegisteredUser {
        val response = client.post("${ApiConstants.AUTH_SERVER}/auth/register/patient") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status != HttpStatusCode.Created) {
            throw apiError(response.status, "No se pudo completar el registro. Inténtalo nuevamente.")
        }
        return response.body()
    }

    suspend fun logout(refreshToken: String) {
        client.post("${ApiConstants.AUTH_SERVER}/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }
    }

    suspend fun createAdminUser(request: AdminUserRequest): RegisteredUser {
        val response = client.post("${ApiConstants.AUTH_SERVER}/auth/admin/users") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (response.status != HttpStatusCode.Created) {
            throw apiError(response.status, "No se pudo crear la cuenta. Inténtalo nuevamente.")
        }
        return response.body()
    }

    suspend fun getRoles(): List<RoleDto> = client.get("${ApiConstants.AUTH_SERVER}/auth/admin/roles").body()

    suspend fun getAvailableProfiles(type: String): List<AvailableProfileDto> {
        val response = client.get("${ApiConstants.AUTH_SERVER}/auth/admin/profiles/available") {
            parameter("type", type)
        }
        if (response.status != HttpStatusCode.OK) {
            throw ApiRequestException(
                statusCode = response.status.value,
                message = "Disponibilidad no consultable (${response.status.value})"
            )
        }
        return response.body()
    }
    suspend fun getUsers(search: String? = null, role: String? = null, status: String? = null, page: Int = 0, size: Int = 20): AdminUserPageDto = client.get("${ApiConstants.AUTH_SERVER}/auth/admin/users") {
        search?.let { parameter("search", it) }; role?.let { parameter("role", it) }; status?.let { parameter("status", it) }; parameter("page", page); parameter("size", size)
    }.body()
    suspend fun getUser(id: Int): AdminUserDto =
        client.get("${ApiConstants.AUTH_SERVER}/auth/admin/users/$id").body()
    suspend fun updateUser(id: Int, request: AdminUserUpdateRequest): AdminUserDto = client.patch("${ApiConstants.AUTH_SERVER}/auth/admin/users/$id") { contentType(ContentType.Application.Json); setBody(request) }.body()
    suspend fun updateUserStatus(id: Int, status: String): AdminUserDto = client.patch("${ApiConstants.AUTH_SERVER}/auth/admin/users/$id/status") { contentType(ContentType.Application.Json); setBody(UserStatusRequest(status)) }.body()
    suspend fun resetPassword(id: Int, temporaryPassword: String) { client.post("${ApiConstants.AUTH_SERVER}/auth/admin/users/$id/reset-password") { contentType(ContentType.Application.Json); setBody(PasswordResetRequest(temporaryPassword)) } }
    suspend fun disableUser(id: Int) { client.delete("${ApiConstants.AUTH_SERVER}/auth/admin/users/$id") }
}
