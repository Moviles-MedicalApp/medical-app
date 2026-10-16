package pe.com.smart.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.navigation.Routes

@Composable
fun AppDrawer(
    currentRoute: String?,
    username: String = "admin",
    role: String = "Administrador",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {

    var showLogoutDialog by
    remember {
        mutableStateOf(false)
    }

    ModalDrawerSheet(
        modifier =
            Modifier.width(300.dp),

        drawerContainerColor =
            MaterialTheme.colorScheme.surface
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(
                        horizontal = 12.dp
                    )
        ) {

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            /*
             * =================================================
             * HEADER
             * =================================================
             */
            DrawerHeader(
                username = username,
                role = role
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            /*
             * =================================================
             * SECCIÓN PRINCIPAL
             * =================================================
             */
            DrawerSectionTitle(
                text =
                    "PRINCIPAL"
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            AppDrawerItem(
                label =
                    "Inicio",

                icon =
                    Icons.Outlined.Home,

                selected =
                    currentRoute == Routes.HOME,

                onClick = {
                    onNavigate(
                        Routes.HOME
                    )
                }
            )

            AppDrawerItem(
                label =
                    "Citas",

                icon =
                    Icons.Outlined.CalendarMonth,

                selected =
                    currentRoute == Routes.APPOINTMENTS,

                onClick = {
                    onNavigate(
                        Routes.APPOINTMENTS
                    )
                }
            )

            AppDrawerItem(
                label =
                    "Pacientes",

                icon =
                    Icons.Outlined.People,

                selected =
                    currentRoute == Routes.PATIENTS,

                onClick = {
                    onNavigate(
                        Routes.PATIENTS
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            /*
             * =================================================
             * SECCIÓN GESTIÓN
             * =================================================
             */
            DrawerSectionTitle(
                text =
                    "GESTIÓN"
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            AppDrawerItem(
                label =
                    "Médicos",

                icon =
                    Icons.Outlined.MedicalServices,

                selected =
                    currentRoute == Routes.DOCTORS,

                onClick = {
                    onNavigate(
                        Routes.DOCTORS
                    )
                }
            )

            AppDrawerItem(
                label =
                    "Especialidades",

                icon =
                    Icons.Outlined.LocalHospital,

                selected =
                    currentRoute == Routes.SPECIALITIES,

                onClick = {
                    onNavigate(
                        Routes.SPECIALITIES
                    )
                }
            )

            AppDrawerItem(
                label =
                    "Agenda médica",

                icon =
                    Icons.Outlined.Schedule,

                selected =
                    currentRoute == Routes.MEDICAL_SCHEDULES,

                onClick = {
                    onNavigate(
                        Routes.MEDICAL_SCHEDULES
                    )
                }
            )

            /*
             * Empuja el cierre de sesión
             * hacia la parte inferior.
             */
            Spacer(
                modifier =
                    Modifier.weight(1f)
            )

            HorizontalDivider(
                color =
                    MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            /*
             * =================================================
             * CERRAR SESIÓN
             * =================================================
             */
            NavigationDrawerItem(
                label = {

                    Text(
                        text =
                            "Cerrar sesión",

                        style =
                            MaterialTheme.typography.bodyLarge,

                        fontWeight =
                            FontWeight.Medium,

                        color =
                            MaterialTheme.colorScheme.error
                    )
                },

                selected =
                    false,

                icon = {

                    Icon(
                        imageVector =
                            Icons.Outlined.Logout,

                        contentDescription =
                            "Cerrar sesión",

                        tint =
                            MaterialTheme.colorScheme.error
                    )
                },

                onClick = {

                    showLogoutDialog =
                        true
                },

                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor =
                            MaterialTheme.colorScheme.surface,

                        unselectedIconColor =
                            MaterialTheme.colorScheme.error,

                        unselectedTextColor =
                            MaterialTheme.colorScheme.error
                    ),

                modifier =
                    Modifier.padding(
                        NavigationDrawerItemDefaults.ItemPadding
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }
    }

    /*
     * =====================================================
     * CONFIRMACIÓN DE LOGOUT
     * =====================================================
     */
    AppConfirmDialog(
        visible =
            showLogoutDialog,

        title =
            "Cerrar sesión",

        message =
            "¿Estás seguro de que deseas cerrar tu sesión actual?",

        confirmText =
            "Cerrar sesión",

        cancelText =
            "Cancelar",

        loadingText =
            "Cerrando...",

        icon =
            Icons.Outlined.Logout,

        destructive =
            false,

        isLoading =
            false,

        onConfirm = {

            showLogoutDialog =
                false

            onLogout()
        },

        onDismiss = {

            showLogoutDialog =
                false
        }
    )
}


/*
 * =====================================================
 * HEADER DEL DRAWER
 * =====================================================
 */

@Composable
private fun DrawerHeader(
    username: String,
    role: String
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        /*
         * Avatar
         */
        Surface(
            modifier =
                Modifier.size(
                    52.dp
                ),

            shape =
                CircleShape,

            color =
                MaterialTheme.colorScheme.primaryContainer
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        getInitials(
                            username
                        ),

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme.colorScheme.primary
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
         * Información de usuario
         */
        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {

            Text(
                text =
                    username,

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme.colorScheme.onSurface,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    role,

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


/*
 * =====================================================
 * TÍTULO DE SECCIÓN
 * =====================================================
 */

@Composable
private fun DrawerSectionTitle(
    text: String
) {

    Text(
        text =
            text,

        modifier =
            Modifier.padding(
                horizontal = 16.dp
            ),

        style =
            MaterialTheme.typography.labelSmall,

        fontWeight =
            FontWeight.SemiBold,

        color =
            MaterialTheme.colorScheme.onSurfaceVariant
    )
}


/*
 * =====================================================
 * ITEM DE NAVEGACIÓN
 * =====================================================
 */

@Composable
private fun AppDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {

    NavigationDrawerItem(
        label = {

            Text(
                text =
                    label,

                style =
                    MaterialTheme.typography.bodyLarge,

                fontWeight =
                    if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
            )
        },

        selected =
            selected,

        icon = {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    label
            )
        },

        onClick =
            onClick,

        colors =
            NavigationDrawerItemDefaults.colors(
                selectedContainerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                selectedIconColor =
                    MaterialTheme.colorScheme.primary,

                selectedTextColor =
                    MaterialTheme.colorScheme.onPrimaryContainer,

                unselectedContainerColor =
                    MaterialTheme.colorScheme.surface,

                unselectedIconColor =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                unselectedTextColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),

        modifier =
            Modifier.padding(
                NavigationDrawerItemDefaults.ItemPadding
            )
    )
}


/*
 * =====================================================
 * INICIALES
 * =====================================================
 */

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