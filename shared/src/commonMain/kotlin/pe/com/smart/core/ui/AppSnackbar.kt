package pe.com.smart.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = 220
                    )
            ) +
                    slideInVertically(
                        animationSpec =
                            tween(
                                durationMillis = 280
                            ),

                        initialOffsetY = {
                            it
                        }
                    ),

        exit =
            fadeOut(
                animationSpec =
                    tween(
                        durationMillis = 180
                    )
            ) +
                    slideOutVertically(
                        animationSpec =
                            tween(
                                durationMillis = 220
                            ),

                        targetOffsetY = {
                            it
                        }
                    )
    ) {

        if (message == null) {
            return@AnimatedVisibility
        }

        val icon =
            getMessageIcon(
                message
            )

        val colors =
            getMessageColors(
                message
            )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    18.dp
                ),

            color =
                colors.container,

            contentColor =
                colors.content,

            tonalElevation =
                2.dp,

            shadowElevation =
                8.dp,

            border =
                BorderStroke(
                    width = 1.dp,

                    color =
                        colors.content.copy(
                            alpha = 0.14f
                        )
                )
        ) {

            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 15.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * =========================================
                 * ICONO
                 * =========================================
                 */
                Surface(
                    modifier =
                        Modifier.size(
                            40.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    color =
                        colors.content.copy(
                            alpha = 0.10f
                        )
                ) {

                    androidx.compose.foundation.layout.Box(
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
                                    24.dp
                                ),

                            tint =
                                colors.content
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            14.dp
                        )
                )

                /*
                 * =========================================
                 * TEXTO
                 * =========================================
                 */
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text =
                            message.title,

                        style =
                            MaterialTheme.typography
                                .titleSmall,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            colors.content
                    )

                    if (
                        !message.message
                            .isNullOrBlank()
                    ) {

                        Text(
                            text =
                                message.message
                                    .orEmpty(),

                            style =
                                MaterialTheme.typography
                                    .bodySmall,

                            color =
                                colors.content.copy(
                                    alpha = 0.82f
                                ),

                            modifier =
                                Modifier.padding(
                                    top = 3.dp
                                )
                        )
                    }
                }
            }
        }
    }
}


/*
 * =====================================================
 * ICONO
 * =====================================================
 */

private fun getMessageIcon(
    message: UiMessage
): ImageVector {

    return when (message) {

        is UiMessage.Success ->
            Icons.Outlined.CheckCircle

        is UiMessage.Error ->
            Icons.Outlined.ErrorOutline

        is UiMessage.Warning ->
            Icons.Outlined.WarningAmber

        is UiMessage.Info ->
            Icons.Outlined.Info
    }
}


/*
 * =====================================================
 * COLORES
 * =====================================================
 */

private data class MessageColors(
    val container: Color,
    val content: Color
)


@Composable
private fun getMessageColors(
    message: UiMessage
): MessageColors {

    val dark =
        isSystemInDarkTheme()

    return when (message) {

        /*
         * SUCCESS
         * Verde
         */
        is UiMessage.Success -> {

            if (dark) {

                MessageColors(
                    container =
                        Color(
                            0xFF163820
                        ),

                    content =
                        Color(
                            0xFFA8DAB5
                        )
                )

            } else {

                MessageColors(
                    container =
                        Color(
                            0xFFE7F6EC
                        ),

                    content =
                        Color(
                            0xFF146C2E
                        )
                )
            }
        }

        /*
         * ERROR
         * Rojo
         */
        is UiMessage.Error -> {

            MessageColors(
                container =
                    MaterialTheme.colorScheme
                        .errorContainer,

                content =
                    MaterialTheme.colorScheme
                        .onErrorContainer
            )
        }

        /*
         * WARNING
         * Ámbar
         */
        is UiMessage.Warning -> {

            if (dark) {

                MessageColors(
                    container =
                        Color(
                            0xFF453208
                        ),

                    content =
                        Color(
                            0xFFFFD67A
                        )
                )

            } else {

                MessageColors(
                    container =
                        Color(
                            0xFFFFF4E5
                        ),

                    content =
                        Color(
                            0xFF8A4B08
                        )
                )
            }
        }

        /*
         * INFO
         * Azul
         */
        is UiMessage.Info -> {

            if (dark) {

                MessageColors(
                    container =
                        Color(
                            0xFF132A46
                        ),

                    content =
                        Color(
                            0xFFAECBFA
                        )
                )

            } else {

                MessageColors(
                    container =
                        Color(
                            0xFFE8F1FF
                        ),

                    content =
                        Color(
                            0xFF0B57D0
                        )
                )
            }
        }
    }
}