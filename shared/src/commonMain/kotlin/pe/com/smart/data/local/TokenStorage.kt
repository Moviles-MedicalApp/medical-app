package pe.com.smart.data.local

import com.russhwolf.settings.Settings

object TokenStorage {

    private const val ACCESS_TOKEN_KEY =
        "access_token"

    private val settings: Settings =
        Settings()

    fun saveToken(token: String) {
        settings.putString(
            ACCESS_TOKEN_KEY,
            token
        )
    }

    fun getToken(): String? {

        return settings.getStringOrNull(
            ACCESS_TOKEN_KEY
        )
    }

    fun hasToken(): Boolean {

        return !getToken().isNullOrBlank()
    }

    fun clearToken() {

        settings.remove(
            ACCESS_TOKEN_KEY
        )
    }
}