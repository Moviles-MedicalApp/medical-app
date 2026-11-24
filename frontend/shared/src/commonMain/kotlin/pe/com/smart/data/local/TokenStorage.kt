package pe.com.smart.data.local

import com.russhwolf.settings.Settings

object TokenStorage {

    private const val ACCESS_TOKEN_KEY =
        "access_token"

    private const val REFRESH_TOKEN_KEY = "refresh_token"

    private val settings: Settings =
        Settings()

    fun saveToken(token: String) {
        settings.putString(
            ACCESS_TOKEN_KEY,
            token
        )
    }

    /** Store both values together so callers never intentionally persist only one. */
    fun saveTokens(accessToken: String, refreshToken: String) {
        settings.putString(ACCESS_TOKEN_KEY, accessToken)
        settings.putString(REFRESH_TOKEN_KEY, refreshToken)
    }

    fun getToken(): String? {

        return settings.getStringOrNull(
            ACCESS_TOKEN_KEY
        )
    }

    fun hasToken(): Boolean {

        return !getToken().isNullOrBlank()
    }

    fun getRefreshToken(): String? = settings.getStringOrNull(REFRESH_TOKEN_KEY)

    fun clearToken() {
        clearTokens()
    }

    fun clearTokens() {
        settings.remove(ACCESS_TOKEN_KEY)
        settings.remove(REFRESH_TOKEN_KEY)
    }
}
