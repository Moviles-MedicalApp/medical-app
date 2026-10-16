package pe.com.smart.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.com.smart.data.local.SessionManager
import pe.com.smart.data.local.TokenStorage

object HttpClientFactory {

    fun create(): HttpClient {

        return HttpClient {

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
             * JWT automático.
             */
            defaultRequest {

                val token =
                    TokenStorage.getToken()

                if (!token.isNullOrBlank()) {

                    header(
                        HttpHeaders.Authorization,
                        "Bearer $token"
                    )
                }
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
                    if (
                        response.status ==
                        HttpStatusCode.Unauthorized
                    ) {

                        val token =
                            TokenStorage.getToken()

                        if (!token.isNullOrBlank()) {

                            SessionManager
                                .notifySessionExpired()
                        }
                    }
                }
            }
        }
    }
}