package pe.com.smart.domain.model

data class Doctor(
    val id: Int,
    val name: String,
    val lastName: String,
    val fullName: String,
    val speciality: String,
    val cmp: String,
    val phone: String,
    val email: String
)
