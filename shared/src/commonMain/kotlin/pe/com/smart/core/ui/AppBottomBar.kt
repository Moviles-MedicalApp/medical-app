package pe.com.smart.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.navigation.Routes

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {

        /*
         * =================================================
         * INICIO
         * =================================================
         */
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,

            onClick = {
                onNavigate(Routes.HOME)
            },

            icon = {

                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Inicio",
                    modifier = Modifier.size(24.dp)
                )
            },

            label = {

                Text(
                    text = "Inicio",

                    style =
                        MaterialTheme.typography.labelMedium,

                    fontWeight =
                        if (
                            currentRoute ==
                            Routes.HOME
                        ) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )
            },

            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.primary,

                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,

                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer,

                    unselectedIconColor =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    unselectedTextColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
        )


        /*
         * =================================================
         * CITAS
         * =================================================
         */
        NavigationBarItem(
            selected =
                currentRoute ==
                        Routes.APPOINTMENTS,

            onClick = {

                onNavigate(
                    Routes.APPOINTMENTS
                )
            },

            icon = {

                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,

                    contentDescription =
                        "Citas",

                    modifier =
                        Modifier.size(24.dp)
                )
            },

            label = {

                Text(
                    text = "Citas",

                    style =
                        MaterialTheme.typography.labelMedium,

                    fontWeight =
                        if (
                            currentRoute ==
                            Routes.APPOINTMENTS
                        ) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )
            },

            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.primary,

                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,

                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer,

                    unselectedIconColor =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    unselectedTextColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
        )


        /*
         * =================================================
         * PACIENTES
         * =================================================
         */
        NavigationBarItem(
            selected =
                currentRoute ==
                        Routes.PATIENTS,

            onClick = {

                onNavigate(
                    Routes.PATIENTS
                )
            },

            icon = {

                Icon(
                    imageVector =
                        Icons.Outlined.People,

                    contentDescription =
                        "Pacientes",

                    modifier =
                        Modifier.size(24.dp)
                )
            },

            label = {

                Text(
                    text = "Pacientes",

                    style =
                        MaterialTheme.typography.labelMedium,

                    fontWeight =
                        if (
                            currentRoute ==
                            Routes.PATIENTS
                        ) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                )
            },

            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.primary,

                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,

                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer,

                    unselectedIconColor =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    unselectedTextColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
        )
    }
}