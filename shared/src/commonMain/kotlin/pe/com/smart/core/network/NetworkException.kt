package pe.com.smart.core.network

class NetworkException(
    override val message: String,
    val statusCode: Int? = null
) : Exception(message)