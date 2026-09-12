package pe.com.smart.data.remote.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String
)