package pe.com.smart.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    showMenu: Boolean = false,
    canNavigateBack: Boolean = false,
    showProfile: Boolean = false,
    username: String = "admin",
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,

                titleContentColor =
                    MaterialTheme.colorScheme.onSurface,

                navigationIconContentColor =
                    MaterialTheme.colorScheme.onSurface,

                actionIconContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),

        navigationIcon = {

            when {

                canNavigateBack -> {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Outlined.ArrowBack,

                            contentDescription =
                                "Volver",

                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }

                showMenu -> {

                    IconButton(
                        onClick = onMenuClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Menu,

                            contentDescription =
                                "Abrir menú",

                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
            }
        },

        title = {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = title,

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight =
                        FontWeight.SemiBold,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {

                    Text(
                        text = subtitle,

                        style =
                            MaterialTheme.typography.labelSmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
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
                    modifier =
                        Modifier.width(12.dp)
                )
            }
        }
    )
}


@Composable
private fun ProfileAvatar(
    username: String,
    onClick: () -> Unit
) {

    val initials =
        getInitials(
            username
        )

    Surface(
        modifier =
            Modifier
                .size(40.dp)
                .clickable(
                    onClick = onClick
                ),

        shape =
            CircleShape,

        color =
            MaterialTheme.colorScheme.primaryContainer,

        contentColor =
            MaterialTheme.colorScheme.onPrimaryContainer
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = initials,

                style =
                    MaterialTheme.typography.labelLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}


private fun getInitials(
    username: String
): String {

    val words =
        username
            .trim()
            .split(" ")
            .filter {
                it.isNotBlank()
            }

    if (words.isEmpty()) {
        return "U"
    }

    if (words.size == 1) {

        return words
            .first()
            .take(2)
            .uppercase()
    }

    return buildString {

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