package pe.com.smart.domain.model

data class Doctor(
    val id: Int,
    val name: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val specialityId: Int,
    val specialityName: String = "",
    val deleted: Boolean = false
) {
    val fullName: String get() = "$name $lastName"
}
