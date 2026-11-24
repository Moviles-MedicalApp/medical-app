
package pe.com.smart.data.local

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

data class AuthTokenClaims(
    val username: String,
    val userId: Int?,
    val roles: Set<String>,
    val expiresAtEpochSeconds: Long?,
    val fullName: String? = null,
    val firstName: String? = null
) {
    fun hasRole(role: String): Boolean =
        role.uppercase() in roles

    fun isExpired(nowEpochSeconds: Long): Boolean =
        expiresAtEpochSeconds?.let {
            it <= nowEpochSeconds
        } ?: true

    val displayName: String
        get() = fullName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: username.ifBlank { "Usuario" }

    val displayFirstName: String
        get() = firstName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: fullName
                ?.trim()
                ?.substringBefore(" ")
                ?.takeIf { it.isNotBlank() }
            ?: username.ifBlank { "Usuario" }
}

/**
 * Lectura de JWT para navegación y presentación.
 * El backend sigue siendo responsable de autorizar.
 */
object AccessTokenClaimsDecoder {

    @OptIn(ExperimentalEncodingApi::class)
    fun decode(
        accessToken: String
    ): AuthTokenClaims? = runCatching {
        val payload = accessToken
            .split('.')
            .getOrNull(1)
            ?: return null

        val padding = "=".repeat(
            (4 - payload.length % 4) % 4
        )

        val json = Base64.UrlSafe
            .decode(payload + padding)
            .decodeToString()

        val claims = Json
            .parseToJsonElement(json)
            .jsonObject

        AuthTokenClaims(
            username = claims["sub"]
                ?.jsonPrimitive
                ?.contentOrNull
                .orEmpty(),

            userId = claims["userId"]
                ?.jsonPrimitive
                ?.intOrNull,

            roles = claims["roles"]
                ?.jsonArray
                ?.mapNotNull {
                    it.jsonPrimitive.contentOrNull
                }
                ?.map {
                    it.removePrefix("ROLE_")
                        .uppercase()
                }
                ?.toSet()
                .orEmpty(),

            expiresAtEpochSeconds = claims["exp"]
                ?.jsonPrimitive
                ?.longOrNull,

            fullName = claims["fullName"]
                ?.jsonPrimitive
                ?.contentOrNull,

            firstName = claims["firstName"]
                ?.jsonPrimitive
                ?.contentOrNull
        )
    }.getOrNull()
}
