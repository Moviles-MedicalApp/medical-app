package pe.com.smart.data.remote.doctor.dt

class DoctorRequestDto (
    val name: String,
    val lastName: String,
    val email: String?,
    val phone: String?,
    val specialityId: Int
){
}