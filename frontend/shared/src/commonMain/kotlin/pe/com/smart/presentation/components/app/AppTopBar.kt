
package pe.com.smart.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Barra superior reutilizable para:
 *
 * - DOCTOR
 * - PATIENT
 * - ADMIN
 *
 * No contiene lógica de autorización.
 * El contenedor proporciona el título,
 * subtítulo y usuario correspondiente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    showMenu: Boolean = false,
    canNavigateBack: Boolean = false,
    showProfile: Boolean = false,
    username: String = "Usuario",
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    CenterAlignedTopAppBar(
        modifier = modifier,

        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },

        navigationIcon = {
            when {
                canNavigateBack -> {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(24.dp),
                            tint = colors.onSurface
                        )
                    }
                }

                showMenu -> {
                    IconButton(
                        onClick = onMenuClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Abrir menú",
                            modifier = Modifier.size(24.dp),
                            tint = colors.onSurface
                        )
                    }
                }
            }
        },

        actions = {
            if (showProfile) {
                ProfileAvatar(
                    username = username,
                    onClick = onProfileClick
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surface,
            titleContentColor = colors.onSurface,
            navigationIconContentColor = colors.onSurface,
            actionIconContentColor = colors.onSurfaceVariant
        )
    )
}

/**
 * Avatar del usuario.
 *
 * Utiliza las iniciales del nombre recibido.
 * El Surface proporciona una superficie
 * interactiva con soporte para accesibilidad.
 */
@Composable
private fun ProfileAvatar(
    username: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = colors.primary.copy(alpha = 0.12f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getInitials(username),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * Genera hasta dos iniciales.
 *
 * "Jimmy Guevara" -> "JG"
 * "alexjc153" -> "AL"
 * "admin" -> "AD"
 * "" -> "U"
 */
private fun getInitials(
    username: String
): String {
    val words = username
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        words.isEmpty() -> {
            "U"
        }

        words.size == 1 -> {
            words.first()
                .take(2)
                .uppercase()
        }

        else -> {
            buildString {
                append(
                    words.first()
                        .first()
                        .uppercaseChar()
                )

                append(
                    words.last()
                        .first()
                        .uppercaseChar()
                )
            }
        }
    }
}
