package pe.com.smart.data.remote.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import pe.com.smart.data.remote.auth.dto.LoginRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse

class AuthApiService(
    private val client: HttpClient
) {

    suspend fun login(
        request: LoginRequest
    ): LoginResponse {

        val response = client.post(
            "https://alexguevara.dev/api/auth-server/auth/login"
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
}