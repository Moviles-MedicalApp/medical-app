package pe.com.smart.domain.model

data class Speciality(
    val id: Int,
    val name: String,
    val description: String,
    val deleted: Boolean = false
)
