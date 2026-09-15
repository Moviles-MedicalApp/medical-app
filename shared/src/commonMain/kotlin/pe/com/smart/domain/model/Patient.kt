package pe.com.smart.domain.model

data class Patient(
    val id: Int,
    val name: String,
    val lastName: String,
    val fullName: String,
    val dni: String,
    val email: String,
    val phone: String
)