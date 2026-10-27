package pe.com.smart.data.remote.doctor.dt

import kotlinx.serialization.Serializable

@Serializable
class DoctorDto (
    val id: Int,
    val name: String,
    val lastName: String,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val specialityId: Int,
    val specialityName: String
    ) {

}