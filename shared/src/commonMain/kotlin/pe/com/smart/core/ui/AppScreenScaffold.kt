package pe.com.smart.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScreenScaffold(
    title: String,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    showBack: Boolean = false,
    showProfile: Boolean = false,
    showBottomBar: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {

    Scaffold(
        modifier = modifier,

        topBar = {
            AppTopBar(
                title = title,
                showMenu = showMenu,
                canNavigateBack = showBack,
                showProfile = showProfile,
                onMenuClick = onMenuClick,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick
            )
        },

        bottomBar = {

            if (showBottomBar) {

                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }
        },

        snackbarHost = {

            snackbarHostState?.let {

                SnackbarHost(
                    hostState = it
                )
            }
        },

        content = content
    )
}