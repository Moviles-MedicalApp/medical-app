package pe.com.smart.data.remote.dto.doctor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoctorRequestDto(
    @SerialName("name")
    val name: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("email")
    val email: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("specialityId")
    val specialityId: Int,
    @SerialName("deleted")
    val deleted: Boolean = false
)
