package pe.com.smart.domain.model

data class MedicalSchedule(
    val id: Int,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val appointmentDuration: Int,
    val doctorId: Int,
    val doctorFullName: String
) {
    val startDisplay: String get() = startTime.take(5)
    val endDisplay: String get() = endTime.take(5)
}
