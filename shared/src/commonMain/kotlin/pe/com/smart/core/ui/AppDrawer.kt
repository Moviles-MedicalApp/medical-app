package pe.com.smart.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.navigation.Routes

@Composable
fun AppDrawer(
    currentRoute: String?,
    username: String = "admin",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
        ) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            DrawerHeader(
                username = username
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            AppDrawerItem(
                label = "Inicio",
                icon = Icons.Outlined.Home,
                selected = currentRoute == Routes.HOME,
                onClick = {
                    onNavigate(Routes.HOME)
                }
            )

            AppDrawerItem(
                label = "Citas",
                icon = Icons.Outlined.CalendarMonth,
                selected = currentRoute == Routes.APPOINTMENTS,
                onClick = {
                    onNavigate(Routes.APPOINTMENTS)
                }
            )

            AppDrawerItem(
                label = "Pacientes",
                icon = Icons.Outlined.People,
                selected = currentRoute == Routes.PATIENTS,
                onClick = {
                    onNavigate(Routes.PATIENTS)
                }
            )

            AppDrawerItem(
                label = "Médicos",
                icon = Icons.Outlined.MedicalServices,
                selected = currentRoute == Routes.DOCTORS,
                onClick = {
                    onNavigate(Routes.DOCTORS)
                }
            )

            AppDrawerItem(
                label = "Especialidades",
                icon = Icons.Outlined.LocalHospital,
                selected = currentRoute == Routes.SPECIALITIES,
                onClick = {
                    onNavigate(Routes.SPECIALITIES)
                }
            )

            AppDrawerItem(
                label = "Agenda médica",
                icon = Icons.Outlined.Schedule,
                selected = currentRoute == Routes.MEDICAL_SCHEDULES,
                onClick = {
                    onNavigate(Routes.MEDICAL_SCHEDULES)
                }
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = onLogout,
                modifier = Modifier.padding(
                    NavigationDrawerItemDefaults.ItemPadding
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    username: String
) {

    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp
        )
    ) {

        Text(
            text = "Medical App",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Sesión: $username",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

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
                text = label,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        },
        selected = selected,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        onClick = onClick,
        modifier = Modifier.padding(
            NavigationDrawerItemDefaults.ItemPadding
        )
    )
}