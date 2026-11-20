package pe.com.smart.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppMessageDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Entendido",
    icon: ImageVector = Icons.Outlined.Info,
    onDismiss: () -> Unit
) {

    if (!visible) {
        return
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        /*
         * =================================================
         * ICONO
         * =================================================
         */
        icon = {

            Surface(
                modifier =
                    Modifier.size(
                        56.dp
                    ),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme
                        .primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            icon,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                28.dp
                            ),

                        tint =
                            MaterialTheme.colorScheme
                                .primary
                    )
                }
            }
        },

        /*
         * =================================================
         * TÍTULO
         * =================================================
         */
        title = {

            Text(
                text =
                    title,

                style =
                    MaterialTheme.typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )
        },

        /*
         * =================================================
         * MENSAJE
         * =================================================
         */
        text = {

            Text(
                text =
                    message,

                style =
                    MaterialTheme.typography
                        .bodyMedium,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        },

        /*
         * =================================================
         * ACCIÓN
         * =================================================
         */
        confirmButton = {

            Button(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        confirmText
                )
            }
        }
    )
}