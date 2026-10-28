package pe.com.smart.data.remote.doctor.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DoctorDto(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("specialityId")
    val specialityId: Int? = null,
    @SerialName("specialityName")
    val specialityName: String? = null,
    @SerialName("deleted")
    val deleted: Boolean? = false
)
