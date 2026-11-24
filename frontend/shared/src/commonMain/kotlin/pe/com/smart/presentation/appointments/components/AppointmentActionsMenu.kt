package pe.com.smart.presentation.appointments.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Acciones contextuales de una cita.
 *
 * Mantiene las acciones junto a la cita que afectan, separa la acción
 * destructiva y usa textos explícitos para reducir errores involuntarios.
 */
@Composable
fun AppointmentActionsMenu(
    appointmentId: Int,
    canEdit: Boolean,
    canConfirm: Boolean,
    canComplete: Boolean,
    canCancel: Boolean,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember(appointmentId) { mutableStateOf(false) }
    val hasPrimaryActions = canEdit || canConfirm || canComplete

    Box {
        IconButton(
            onClick = { expanded = true },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Ver acciones de la cita"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = colors.surfaceContainerHigh,
            tonalElevation = androidx.compose.ui.unit.Dp.Hairline,
            shadowElevation = androidx.compose.ui.unit.Dp.Hairline
        ) {
            if (canEdit) {
                AppointmentMenuItem(
                    label = "Reprogramar cita",
                    icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onEdit()
                    }
                )
            }

            if (canConfirm) {
                AppointmentMenuItem(
                    label = "Confirmar cita",
                    icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onConfirm()
                    }
                )
            }

            if (canComplete) {
                AppointmentMenuItem(
                    label = "Completar atención",
                    icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onComplete()
                    }
                )
            }

            if (canCancel) {
                if (hasPrimaryActions) {
                    HorizontalDivider(color = colors.outlineVariant)
                }
                AppointmentMenuItem(
                    label = "Cancelar cita",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = colors.error
                        )
                    },
                    contentColor = colors.error,
                    onClick = {
                        expanded = false
                        onCancel()
                    }
                )
            }
        }
    }
}

@Composable
private fun AppointmentMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = icon,
        onClick = onClick
    )
}
