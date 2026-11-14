package pe.com.smart.presentation.appointments.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared, read-only representation of an appointment state across role-specific screens. */
@Composable
fun AppointmentStatusBadge(status: String) {
    val (container, content) = when (status) {
        "COMPLETED" -> Color(0xFFE4F6EA) to Color(0xFF177A39)
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        "CONFIRMED" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            appointmentStatusLabel(status),
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

fun appointmentStatusLabel(value: String) = when (value) {
    "SCHEDULED" -> "Programada"
    "CONFIRMED" -> "Confirmada"
    "COMPLETED" -> "Completada"
    "CANCELLED" -> "Cancelada"
    else -> value
}
