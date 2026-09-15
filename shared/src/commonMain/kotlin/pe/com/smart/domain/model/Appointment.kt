package pe.com.smart.domain.model

data class Appointment(
    val id: Int,
    val patientId: Int,
    val doctorId: Int,
    val reason: String,
    val appointmentDate: String,
    val startTime: String,
    val endTime: String,
    val status: String
)