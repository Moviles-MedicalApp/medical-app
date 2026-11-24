
package pe.com.smart.presentation.session

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import pe.com.smart.core.network.ApiConstants
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.HttpClientFactory

@Serializable
data class SessionProfile(
    val id: Int,
    val name: String,
    val lastName: String,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dni: String? = null,
    val specialityId: Int? = null,
    val specialityName: String? = null
) {
    val displayName: String
        get() = fullName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "$name $lastName"
                .trim()
                .ifBlank { "Usuario" }

    val firstName: String
        get() = name
            .trim()
            .substringBefore(" ")
            .ifBlank {
                displayName.substringBefore(" ")
            }
}

data class SessionIdentityUiState(
    val profile: SessionProfile? = null,
    val role: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val displayName: String?
        get() = profile?.displayName

    val firstName: String?
        get() = profile?.firstName
}

/**
 * Fuente única de identidad para Dashboard y Drawer.
 *
 * PATIENT -> GET /medical-appointment/patients/me
 * DOCTOR  -> GET /medical-appointment/doctors/me
 *
 * ADMIN y EMPLOYEE utilizan los datos del JWT
 * hasta disponer de un endpoint de identidad.
 */
object SessionIdentityStore {

    private val client = HttpClientFactory.create()
    private val mutex = Mutex()

    private val _uiState = MutableStateFlow(
        SessionIdentityUiState()
    )

    val uiState: StateFlow<SessionIdentityUiState> =
        _uiState.asStateFlow()

    private var loadedToken: String? = null
    private var loadedRole: String? = null
    private var loadAttempted: Boolean = false

    private val medicalBaseUrl: String
        get() {
            val authBase =
                ApiConstants.AUTH_SERVER.trimEnd('/')

            val gatewayBase =
                authBase.removeSuffix("/auth-server")

            return "$gatewayBase/medical-appointment"
        }

    private fun resolveRole(token: String): String? {
        val claims =
            AccessTokenClaimsDecoder.decode(token)

        return when {
            claims?.hasRole("ADMIN") == true ->
                "ADMIN"

            claims?.hasRole("DOCTOR") == true ->
                "DOCTOR"

            claims?.hasRole("PATIENT") == true ->
                "PATIENT"

            claims?.hasRole("EMPLOYEE") == true ->
                "EMPLOYEE"

            else -> null
        }
    }

    suspend fun ensureLoaded() {
        load(force = false)
    }

    suspend fun refresh() {
        load(force = true)
    }

    private suspend fun load(force: Boolean) {
        val token = TokenStorage.getToken().orEmpty()
        val role = resolveRole(token)

        if (
            token.isBlank() ||
            role !in setOf("PATIENT", "DOCTOR")
        ) {
            clear()
            return
        }

        mutex.withLock {
            val currentToken =
                TokenStorage.getToken().orEmpty()

            if (currentToken != token) {
                return@withLock
            }

            if (
                !force &&
                loadedToken == token &&
                loadedRole == role &&
                loadAttempted
            ) {
                return@withLock
            }

            loadedToken = token
            loadedRole = role
            loadAttempted = true

            val previousProfile =
                _uiState.value.profile
                    ?.takeIf {
                        !force &&
                                loadedToken == token
                    }

            _uiState.value = SessionIdentityUiState(
                profile = if (force) {
                    previousProfile
                } else {
                    null
                },
                role = role,
                isLoading = true
            )

            val path = when (role) {
                "DOCTOR" -> "doctors/me"
                "PATIENT" -> "patients/me"
                else -> return@withLock
            }

            try {
                val response = client.get(
                    "$medicalBaseUrl/$path"
                ) {
                    header(
                        HttpHeaders.Authorization,
                        "Bearer $token"
                    )
                }

                if (
                    TokenStorage.getToken() != token ||
                    loadedToken != token
                ) {
                    return@withLock
                }

                if (response.status == HttpStatusCode.OK) {
                    val profile =
                        response.body<SessionProfile>()

                    _uiState.value = SessionIdentityUiState(
                        profile = profile,
                        role = role,
                        isLoading = false
                    )
                } else {
                    _uiState.value = SessionIdentityUiState(
                        role = role,
                        isLoading = false,
                        error = when (response.status) {
                            HttpStatusCode.NotFound ->
                                "No existe un perfil activo vinculado."

                            HttpStatusCode.Unauthorized ->
                                "La sesión no es válida."

                            HttpStatusCode.Forbidden ->
                                "No tienes permiso para consultar el perfil."

                            else ->
                                "No se pudo obtener el perfil. HTTP ${response.status.value}."
                        }
                    )
                }

            } catch (exception: CancellationException) {
                loadAttempted = false

                if (TokenStorage.getToken() == token) {
                    _uiState.value = SessionIdentityUiState(
                        role = role
                    )
                }

                throw exception

            } catch (exception: Exception) {
                if (TokenStorage.getToken() == token) {
                    _uiState.value = SessionIdentityUiState(
                        role = role,
                        isLoading = false,
                        error = exception.message
                            ?: "Error al obtener el perfil."
                    )
                }
            }
        }
    }

    fun clear() {
        loadedToken = null
        loadedRole = null
        loadAttempted = false

        _uiState.value = SessionIdentityUiState()
    }
}
