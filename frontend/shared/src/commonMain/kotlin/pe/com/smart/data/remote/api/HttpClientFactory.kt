package pe.com.smart.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.com.smart.data.local.SessionManager
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.remote.auth.dto.RefreshRequest
import pe.com.smart.data.remote.auth.dto.LoginResponse

object HttpClientFactory {

    private val refreshMutex = Mutex()

    private fun isAuthEndpoint(path: String): Boolean =
        path.endsWith("/auth/login") ||
            path.endsWith("/auth/register/patient") ||
            path.endsWith("/auth/refresh") ||
            path.endsWith("/auth/logout")

    private fun isAuthRequest(request: HttpRequestBuilder): Boolean =
        isAuthEndpoint(request.url.build().encodedPath)

    fun create(): HttpClient {
        lateinit var httpClient: HttpClient
        httpClient = HttpClient {

            /*
             * JSON
             */
            install(ContentNegotiation) {

                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }

            /*
             * Manejo global de respuestas.
             */
            HttpResponseValidator {

                validateResponse { response ->

                    /*
                     * IMPORTANTE:
                     *
                     * Solamente consideramos el 401
                     * como sesión expirada cuando
                     * ya existe un token guardado.
                     *
                     * De esta manera un login con
                     * contraseña incorrecta no
                     * dispara SessionManager.
                     */
                    // 401 recovery is handled by HttpSend above. Keeping this
                    // validator passive prevents login failures from clearing state.
                }
            }
        }

        /* HttpSend is an always-installed Ktor plugin. In 3.5 its Config has no
         * intercept function, therefore this hook must be registered after the
         * client has been created. */
        httpClient.plugin(HttpSend).intercept { request ->
            if (!isAuthRequest(request)) {
                request.headers.remove(HttpHeaders.Authorization)
                TokenStorage.getToken()?.let {
                    request.headers.append(HttpHeaders.Authorization, "Bearer $it")
                }
            }
            val firstCall = execute(request)
            if (firstCall.response.status != HttpStatusCode.Unauthorized ||
                isAuthRequest(request) || request.headers["X-Auth-Retry"] == "true"
            ) return@intercept firstCall

            val refreshed = refreshMutex.withLock {
                val originalToken = request.headers[HttpHeaders.Authorization]
                if (TokenStorage.getToken()?.let { "Bearer $it" } != originalToken) true
                else {
                    val refreshToken = TokenStorage.getRefreshToken()
                    if (refreshToken.isNullOrBlank()) false else try {
                        val response = httpClient.post("${pe.com.smart.core.network.ApiConstants.AUTH_SERVER}/auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken))
                        }
                        if (response.status == HttpStatusCode.OK) {
                            response.body<LoginResponse>().also {
                                TokenStorage.saveTokens(it.accessToken, it.refreshToken)
                            }
                            true
                        } else false
                    } catch (_: Exception) { false }
                }
            }
            if (!refreshed) {
                SessionManager.notifySessionExpired()
                return@intercept firstCall
            }
            request.headers.remove(HttpHeaders.Authorization)
            TokenStorage.getToken()?.let { request.headers.append(HttpHeaders.Authorization, "Bearer $it") }
            request.headers.append("X-Auth-Retry", "true")
            execute(request)
        }
        return httpClient
    }
}
