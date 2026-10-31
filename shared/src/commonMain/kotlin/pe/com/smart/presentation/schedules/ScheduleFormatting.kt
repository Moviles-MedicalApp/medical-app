package pe.com.smart.presentation.schedules

import pe.com.smart.domain.model.MedicalSchedule

internal val weekDays = listOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")
internal val dayLabels = mapOf("SUNDAY" to "Domingo", "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles", "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado")
internal fun MedicalSchedule.hours(): Int {
    fun minutes(value: String): Int { val parts = value.split(":"); return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0) }
    return ((minutes(endTime) - minutes(startTime)).coerceAtLeast(0)) / 60
}
internal fun MedicalSchedule.isMorning() = (startTime.take(2).toIntOrNull() ?: 0) < 12
internal fun MedicalSchedule.appointmentCount(): Int {
    fun minutes(value: String): Int { val parts = value.split(":"); return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0) }
    return ((minutes(endTime) - minutes(startTime)).coerceAtLeast(0) / appointmentDuration.coerceAtLeast(1))
}
