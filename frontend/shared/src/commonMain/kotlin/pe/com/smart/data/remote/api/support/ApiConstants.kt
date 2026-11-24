package pe.com.smart.data.remote.api.support

object ApiConstants {

    /** Gateway URL. Override this value per build/environment, never point mobile
     * clients at an individual microservice port. */
    const val BASE_URL = "https://alexguevara.dev"

    const val AUTH_SERVER =
        "$BASE_URL/auth-server"

    const val MEDICAL_APPOINTMENT =
        "$BASE_URL/medical-appointment"

    const val CONSULTATIONS =
        "$BASE_URL/consultations-ms"
}
