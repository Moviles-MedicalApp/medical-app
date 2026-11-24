package pe.com.smart.data.remote.api.support

class NetworkException(
    override val message: String,
    val statusCode: Int? = null
) : Exception(message)