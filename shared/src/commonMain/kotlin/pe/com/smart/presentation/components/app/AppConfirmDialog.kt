
package pe.com.smart.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Diálogo de confirmación reutilizable.
 *
 * Compatible con Android y iOS mediante
 * Compose Multiplatform + Material 3.
 *
 * destructive = true:
 *     Operación con consecuencias importantes,
 *     como eliminar o desactivar un registro.
 *
 * isLoading = true:
 *     Impide cerrar o confirmar repetidamente
 *     mientras se ejecuta una operación.
 */
@Composable
fun AppConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    loadingText: String = "Procesando...",
    icon: ImageVector = Icons.Outlined.DeleteOutline,
    destructive: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val colors = MaterialTheme.colorScheme

    val iconBackground = if (destructive) {
        colors.errorContainer
    } else {
        colors.primaryContainer
    }

    val iconColor = if (destructive) {
        colors.onErrorContainer
    } else {
        colors.onPrimaryContainer
    }

    val buttonColors = if (destructive) {
        ButtonDefaults.buttonColors(
            containerColor = colors.error,
            contentColor = colors.onError
        )
    } else {
        ButtonDefaults.buttonColors()
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        containerColor = colors.surface,

        iconContentColor = iconColor,

        titleContentColor = colors.onSurface,

        textContentColor = colors.onSurfaceVariant,

        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = iconBackground
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = iconColor
                    )
                }
            }
        },

        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },

        text = {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = buttonColors,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = if (destructive) {
                                colors.onError
                            } else {
                                colors.onPrimary
                            }
                        )
                    }

                    Text(
                        text = if (isLoading) {
                            loadingText
                        } else {
                            confirmText
                        },
                        style = MaterialTheme
                            .typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },

        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = cancelText,
                    style = MaterialTheme
                        .typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
