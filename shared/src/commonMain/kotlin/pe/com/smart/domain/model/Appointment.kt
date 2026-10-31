package pe.com.smart.domain.model

data class Appointment(
    val id: Int,
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val patientId: Int,
    val patientFullName: String,
    val patientDni: String,
    val doctorId: Int,
    val doctorFullName: String,
    val specialityId: Int? = null,
    val specialityName: String = ""
) {
    val timeRange get() = "${startTime.take(5)}–${endTime.take(5)}"
}
