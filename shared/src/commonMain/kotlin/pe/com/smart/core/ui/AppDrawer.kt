
package pe.com.smart.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule

import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import kotlinx.coroutines.launch

import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.presentation.navigation.Routes
import pe.com.smart.presentation.session.SessionIdentityStore

private data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private data class DrawerMenuSection(
    val title: String,
    val items: List<DrawerMenuItem>
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    username: String = "Usuario",
    role: String = "Usuario",
    isAdministrator: Boolean = false,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    drawerState: DrawerState? = null
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    val token = TokenStorage.getToken().orEmpty()

    val claims = AccessTokenClaimsDecoder.decode(token)

    val identity by SessionIdentityStore.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        SessionIdentityStore.ensureLoaded()
    }

    val isAdmin =
        isAdministrator &&
                claims?.hasRole("ADMIN") == true

    val isDoctor =
        !isAdmin &&
                claims?.hasRole("DOCTOR") == true

    val isPatient =
        !isAdmin &&
                !isDoctor &&
                claims?.hasRole("PATIENT") == true

    val isEmployee =
        !isAdmin &&
                !isDoctor &&
                !isPatient &&
                claims?.hasRole("EMPLOYEE") == true

    val displayName = when {
        (isDoctor || isPatient) &&
                identity.profile != null -> {
            identity.profile!!.displayName
        }

        else -> {
            claims?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: username.ifBlank { "Usuario" }
        }
    }

    val sections = buildDrawerSections(
        isAdmin = isAdmin,
        isDoctor = isDoctor,
        isPatient = isPatient,
        isEmployee = isEmployee
    )

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    var closingDrawer by remember {
        mutableStateOf(false)
    }

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = colors.surface,
        drawerContentColor = colors.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(
                modifier = Modifier.height(24.dp)
            )

            DrawerHeader(
                displayName = displayName,
                role = role
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                sections.forEach { section ->
                    item(
                        key = "section_${section.title}"
                    ) {
                        DrawerSectionTitle(
                            title = section.title
                        )
                    }

                    items(
                        items = section.items,
                        key = { it.route }
                    ) { menuItem ->
                        AppDrawerMenuItem(
                            item = menuItem,
                            selected =
                                currentRoute == menuItem.route,
                            onClick = {
                                onNavigate(menuItem.route)
                            }
                        )
                    }

                    item(
                        key = "space_${section.title}"
                    ) {
                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Cerrar sesión",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {
                    if (!closingDrawer) {
                        closingDrawer = true

                        scope.launch {
                            try {
                                drawerState?.close()
                                showLogoutDialog = true
                            } finally {
                                closingDrawer = false
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor =
                            colors.surface,
                        unselectedIconColor =
                            colors.error,
                        unselectedTextColor =
                            colors.error
                    ),
                modifier = Modifier.padding(
                    NavigationDrawerItemDefaults.ItemPadding
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }

    AppConfirmDialog(
        visible = showLogoutDialog,
        title = "¿Cerrar sesión?",
        message = "Tendrás que volver a iniciar sesión para acceder a MedicalApp.",
        confirmText = "Cerrar sesión",
        cancelText = "Cancelar",
        loadingText = "Cerrando sesión...",
        icon = Icons.Outlined.Logout,
        destructive = false,
        isLoading = false,
        onConfirm = {
            showLogoutDialog = false
            SessionIdentityStore.clear()
            onLogout()
        },
        onDismiss = {
            showLogoutDialog = false
        }
    )
}

private fun buildDrawerSections(
    isAdmin: Boolean,
    isDoctor: Boolean,
    isPatient: Boolean,
    isEmployee: Boolean
): List<DrawerMenuSection> {
    val mainItems = mutableListOf(
        DrawerMenuItem(
            route = Routes.HOME,
            label = "Inicio",
            icon = Icons.Outlined.Home
        )
    )

    if (isAdmin) {
        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENTS,
                label = "Citas",
                icon = Icons.Outlined.CalendarMonth
            )
        )

        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENT_CALENDAR,
                label = "Calendario",
                icon = Icons.Outlined.DateRange
            )
        )

        mainItems.add(
            DrawerMenuItem(
                route = Routes.PATIENTS,
                label = "Pacientes",
                icon = Icons.Outlined.People
            )
        )
    }

    if (isDoctor) {
        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENTS,
                label = "Mi agenda",
                icon = Icons.Outlined.CalendarMonth
            )
        )
    }

    if (isPatient) {
        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENTS,
                label = "Mis citas",
                icon = Icons.Outlined.CalendarMonth
            )
        )

        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENT_CALENDAR,
                label = "Calendario",
                icon = Icons.Outlined.DateRange
            )
        )
    }

    if (isEmployee) {
        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENTS,
                label = "Citas",
                icon = Icons.Outlined.CalendarMonth
            )
        )

        mainItems.add(
            DrawerMenuItem(
                route = Routes.APPOINTMENT_CALENDAR,
                label = "Calendario",
                icon = Icons.Outlined.DateRange
            )
        )
    }

    val sections = mutableListOf(
        DrawerMenuSection(
            title = "PRINCIPAL",
            items = mainItems
        )
    )

    if (isAdmin) {
        sections.add(
            DrawerMenuSection(
                title = "GESTIÓN",
                items = listOf(
                    DrawerMenuItem(
                        route = Routes.USERS,
                        label = "Usuarios",
                        icon = Icons.Outlined.People
                    ),
                    DrawerMenuItem(
                        route = Routes.DOCTORS,
                        label = "Médicos",
                        icon = Icons.Outlined.MedicalServices
                    ),
                    DrawerMenuItem(
                        route = Routes.SPECIALITIES,
                        label = "Especialidades",
                        icon = Icons.Outlined.LocalHospital
                    ),
                    DrawerMenuItem(
                        route = Routes.MEDICAL_SCHEDULES,
                        label = "Horarios médicos",
                        icon = Icons.Outlined.Schedule
                    )
                )
            )
        )
    }

    return sections
}

@Composable
private fun DrawerHeader(
    displayName: String,
    role: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            border = BorderStroke(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.12f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drawerInitials(displayName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.primaryContainer
            ) {
                Text(
                    text = role,
                    modifier = Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 4.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionTitle(
    title: String
) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 6.dp
        ),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AppDrawerMenuItem(
    item: DrawerMenuItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    NavigationDrawerItem(
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Medium
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        selected = selected,
        icon = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor =
                colors.primaryContainer,
            selectedIconColor =
                colors.primary,
            selectedTextColor =
                colors.onPrimaryContainer,
            unselectedContainerColor =
                colors.surface,
            unselectedIconColor =
                colors.onSurfaceVariant,
            unselectedTextColor =
                colors.onSurface
        ),
        modifier = Modifier.padding(
            NavigationDrawerItemDefaults.ItemPadding
        )
    )
}

private fun drawerInitials(
    displayName: String
): String {
    val words = displayName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when (words.size) {
        0 -> "U"

        1 -> words.first()
            .take(2)
            .uppercase()

        else -> buildString {
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
