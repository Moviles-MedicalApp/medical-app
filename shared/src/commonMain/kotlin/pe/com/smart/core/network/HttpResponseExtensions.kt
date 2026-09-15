package pe.com.smart.core.network

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

fun HttpResponse.ensureSuccess(
    defaultMessage: String
) {

    when (status) {

        HttpStatusCode.OK,
        HttpStatusCode.Created,
        HttpStatusCode.NoContent -> Unit

        HttpStatusCode.BadRequest -> {
            throw NetworkException(
                message = "Los datos enviados no son válidos.",
                statusCode = 400
            )
        }

        HttpStatusCode.Unauthorized -> {
            throw NetworkException(
                message = "Tu sesión ha expirado. Inicia sesión nuevamente.",
                statusCode = 401
            )
        }

        HttpStatusCode.Forbidden -> {
            throw NetworkException(
                message = "No tienes permisos para realizar esta operación.",
                statusCode = 403
            )
        }

        HttpStatusCode.NotFound -> {
            throw NetworkException(
                message = "No se encontró el recurso solicitado.",
                statusCode = 404
            )
        }

        HttpStatusCode.Conflict -> {
            throw NetworkException(
                message = "La información ingresada ya se encuentra registrada.",
                statusCode = 409
            )
        }

        HttpStatusCode.InternalServerError -> {
            throw NetworkException(
                message = "Ocurrió un error interno en el servidor.",
                statusCode = 500
            )
        }

        else -> {
            throw NetworkException(
                message = defaultMessage,
                statusCode = status.value
            )
        }
    }
}