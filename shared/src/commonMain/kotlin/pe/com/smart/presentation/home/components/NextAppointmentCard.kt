
package pe.com.smart.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.home.AppointmentSummary
import pe.com.smart.presentation.appointments.components.AppointmentStatusBadge

/**
 * Tarjeta reutilizable para mostrar la próxima cita.
 *
 * Compatible con DOCTOR, PATIENT y ADMIN.
 *
 * isDoctor:
 * true  -> Muestra principalmente al paciente.
 * false -> Muestra al médico y la especialidad.
 */
@Composable
fun NextAppointmentCard(
    appointment: AppointmentSummary?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isDoctor: Boolean = false
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        if (appointment == null) {
            EmptyAppointmentContent()
        } else {
            AppointmentContent(
                appointment = appointment,
                isDoctor = isDoctor
            )
        }
    }
}

@Composable
private fun AppointmentContent(
    appointment: AppointmentSummary,
    isDoctor: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = formatAppointmentDateTime(
                        appointment.time
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 2
                )

                Text(
                    text = if (isDoctor) {
                        "Próxima atención"
                    } else {
                        "Próxima consulta"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            AppointmentStatusBadge(appointment.status)
        }

        HorizontalDivider(
            color = colors.outlineVariant
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppointmentInfoRow(
                icon = Icons.Outlined.PersonOutline,
                label = if (isDoctor) "Paciente" else "Médico",
                value = if (isDoctor) {
                    appointment.patientName
                } else {
                    appointment.doctorName
                }
            )

            if (appointment.speciality.isNotBlank()) {
                AppointmentInfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Especialidad",
                    value = appointment.speciality
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDoctor) "Ver agenda" else "Ver detalle",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )

            Spacer(
                modifier = Modifier.size(4.dp)
            )

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AppointmentInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.onSurfaceVariant
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            Text(
                text = value.ifBlank {
                    "No especificado"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyAppointmentContent() {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            color = colors.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventAvailable,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "No hay próximas citas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Text(
                text = "Cuando programes una atención, aparecerá aquí.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * Formatea el texto recibido desde HomeViewModel.
 *
 * Entrada:
 * 2026-11-18 · 08:00–08:30
 *
 * Salida:
 * 18 de noviembre de 2026 · 08:00–08:30
 *
 * Si no reconoce el formato, devuelve
 * el valor original.
 */
private fun formatAppointmentDateTime(
    value: String
): String {
    val parts = value.split(" · ", limit = 2)

    if (parts.size != 2) {
        return value
    }

    val dateParts = parts[0].split("-")

    if (dateParts.size != 3) {
        return value
    }

    val year = dateParts[0].toIntOrNull()
        ?: return value

    val month = dateParts[1].toIntOrNull()
        ?: return value

    val day = dateParts[2].toIntOrNull()
        ?: return value

    val months = listOf(
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "septiembre",
        "octubre",
        "noviembre",
        "diciembre"
    )

    if (month !in 1..12 || day !in 1..31) {
        return value
    }

    val formattedDate = buildString {
        append(day)
        append(" de ")
        append(months[month - 1])
        append(" de ")
        append(year)
    }

    return "$formattedDate · ${parts[1]}"
}
