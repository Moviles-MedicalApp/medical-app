
package pe.com.smart.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Contenedor visual común de la aplicación.
 *
 * Responsabilidades:
 *
 * - Mostrar AppTopBar.
 * - Mostrar AppBottomBar cuando corresponda.
 * - Gestionar el área visual del contenido.
 * - Integrar SnackbarHost opcional.
 * - Aplicar el fondo de MaterialTheme.
 *
 * Compatible con los roles:
 * ADMIN, DOCTOR y PATIENT.
 */
@Composable
fun AppScreenScaffold(
    title: String,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    showBack: Boolean = false,
    showProfile: Boolean = false,
    showBottomBar: Boolean = false,
    isAdministrator: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    subtitle: String? = null,
    username: String = "Usuario",
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {
            AppTopBar(
                title = title,
                subtitle = subtitle,
                showMenu = showMenu,
                canNavigateBack = showBack,
                showProfile = showProfile,
                username = username,
                onMenuClick = onMenuClick,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },

        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    isAdministrator = isAdministrator,
                    onNavigate = onNavigate
                )
            }
        },

        snackbarHost = {
            snackbarHostState?.let { state ->
                SnackbarHost(
                    hostState = state
                )
            }
        },

        content = content
    )
}
