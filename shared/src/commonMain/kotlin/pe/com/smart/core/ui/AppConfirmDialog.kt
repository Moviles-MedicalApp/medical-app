package pe.com.smart.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

    if (!visible) {
        return
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },

        icon = {

            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color =
                    if (destructive) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint =
                            if (destructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                    )
                }
            }
        },

        title = {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },

        text = {

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },

        confirmButton = {

            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors =
                    if (destructive) {
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.error,
                            contentColor =
                                MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color =
                            if (destructive) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )
                }

                Text(
                    text =
                        if (isLoading) {
                            loadingText
                        } else {
                            confirmText
                        }
                )
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {

                Text(
                    text = cancelText
                )
            }
        }
    )
}