package pe.com.smart.presentation.appointments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Shared, read-only representation of an appointment state across role-specific screens. */
data class AppointmentStatusVisual(
    val container: Color,
    val content: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun appointmentStatusVisual(status: String): AppointmentStatusVisual = when (status.uppercase()) {
    // Estados con significado consistente en toda la aplicación:
    // pendiente = ámbar, confirmado = azul, completado = verde,
    // cancelado = rojo. El icono y texto evitan depender solo del color.
    "SCHEDULED" -> AppointmentStatusVisual(
        Color(0xFFFFF1CF),
        Color(0xFF875A00),
        Icons.Outlined.Schedule
    )
    "CONFIRMED" -> AppointmentStatusVisual(
        Color(0xFFDCEEFF),
        Color(0xFF00639B),
        Icons.Outlined.CheckCircle
    )
    "COMPLETED" -> AppointmentStatusVisual(
        Color(0xFFE2F5E8),
        Color(0xFF176B3A),
        Icons.Outlined.DoneAll
    )
    "CANCELLED" -> AppointmentStatusVisual(
        Color(0xFFFDE7E7),
        Color(0xFFB42318),
        Icons.Outlined.Cancel
    )
    else -> AppointmentStatusVisual(
        Color(0xFFF1F5F9),
        Color(0xFF475569),
        Icons.Outlined.Schedule
    )
}

@Composable
fun AppointmentStatusBadge(status: String) {
    val visual = appointmentStatusVisual(status)

    Surface(
        shape = RoundedCornerShape(50),
        color = visual.container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = visual.content
            )
            Text(
                appointmentStatusLabel(status),
                style = MaterialTheme.typography.labelSmall,
                color = visual.content
            )
        }
    }
}

fun appointmentStatusLabel(value: String) = when (value.uppercase()) {
    "SCHEDULED" -> "Programada"
    "CONFIRMED" -> "Confirmada"
    "COMPLETED" -> "Completada"
    "CANCELLED" -> "Cancelada"
    else -> value
}
