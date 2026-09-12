package pe.com.smart.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
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
import pe.com.smart.core.ui.message.UiMessage

@Composable
fun AppSnackbar(
    message: UiMessage?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,

        enter =
            fadeIn() +
                    slideInVertically(
                        initialOffsetY = {
                            it / 2
                        }
                    ),

        exit =
            fadeOut() +
                    slideOutVertically(
                        targetOffsetY = {
                            it / 2
                        }
                    )
    ) {

        /*
         * Si no existe mensaje, no renderizamos
         * contenido dentro de la animación.
         */
        if (message == null) {
            return@AnimatedVisibility
        }

        /*
         * =================================================
         * ICONO SEGÚN TIPO DE MENSAJE
         * =================================================
         */
        val icon: ImageVector =
            when (message) {

                is UiMessage.Success ->
                    Icons.Outlined.CheckCircle

                is UiMessage.Error ->
                    Icons.Outlined.ErrorOutline

                is UiMessage.Info ->
                    Icons.Outlined.Info

                is UiMessage.Warning ->
                    Icons.Outlined.WarningAmber
            }

        /*
         * =================================================
         * COLOR DE FONDO SEGÚN ACCIÓN
         * =================================================
         */
        val containerColor =
            when (message) {

                is UiMessage.Success ->
                    MaterialTheme.colorScheme.primaryContainer

                is UiMessage.Error ->
                    MaterialTheme.colorScheme.errorContainer

                is UiMessage.Info ->
                    MaterialTheme.colorScheme.secondaryContainer

                is UiMessage.Warning ->
                    MaterialTheme.colorScheme.tertiaryContainer
            }

        /*
         * =================================================
         * COLOR DEL CONTENIDO
         * =================================================
         */
        val contentColor =
            when (message) {

                is UiMessage.Success ->
                    MaterialTheme.colorScheme.onPrimaryContainer

                is UiMessage.Error ->
                    MaterialTheme.colorScheme.onErrorContainer

                is UiMessage.Info ->
                    MaterialTheme.colorScheme.onSecondaryContainer

                is UiMessage.Warning ->
                    MaterialTheme.colorScheme.onTertiaryContainer
            }

        /*
         * =================================================
         * CONTENEDOR DE LA NOTIFICACIÓN
         * =================================================
         */
        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(18.dp),

            color =
                containerColor,

            contentColor =
                contentColor,

            tonalElevation =
                4.dp,

            shadowElevation =
                6.dp,

            border =
                BorderStroke(
                    width = 1.dp,

                    color =
                        contentColor.copy(
                            alpha = 0.12f
                        )
                )
        ) {

            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * ICONO
                 */
                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(28.dp),

                    tint =
                        contentColor
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                /*
                 * TEXTO
                 */
                Column(
                    modifier =
                        Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    /*
                     * TÍTULO
                     */
                    Text(
                        text =
                            message.title,

                        style =
                            MaterialTheme.typography.titleSmall,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            contentColor
                    )

                    /*
                     * MENSAJE OPCIONAL
                     */
                    if (
                        !message.message.isNullOrBlank()
                    ) {

                        Text(
                            text =
                                message.message.orEmpty(),

                            style =
                                MaterialTheme.typography.bodySmall,

                            color =
                                contentColor.copy(
                                    alpha = 0.82f
                                ),

                            modifier =
                                Modifier.padding(
                                    top = 2.dp
                                )
                        )
                    }
                }
            }
        }
    }
}