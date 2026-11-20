
package pe.com.smart.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.presentation.navigation.Routes

/**
 * Modelo visual de cada opción del BottomBar.
 *
 * No contiene lógica de navegación ni permisos.
 */
private data class BottomNavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Barra de navegación inferior de MedicalApp.
 *
 * Opciones según el rol:
 *
 * DOCTOR:
 * - Inicio
 * - Mi agenda
 *
 * PATIENT:
 * - Inicio
 * - Mis citas
 * - Calendario
 *
 * ADMIN:
 * - Inicio
 * - Citas
 * - Calendario
 * - Pacientes
 *
 * La autorización definitiva debe ser
 * validada también por el backend.
 */
@Composable
fun AppBottomBar(
    currentRoute: String?,
    isAdministrator: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    // =====================================================
    // SESSION
    // =====================================================

    val claims = AccessTokenClaimsDecoder.decode(
        TokenStorage.getToken().orEmpty()
    )

    // =====================================================
    // ROLES
    // =====================================================

    /*
     * Se respeta la prioridad del rol ADMIN
     * utilizada por AppNavigation.
     */
    val isAdmin = isAdministrator &&
            claims?.hasRole("ADMIN") == true

    val isDoctor = !isAdmin &&
            claims?.hasRole("DOCTOR") == true

    val isPatient = !isAdmin &&
            !isDoctor &&
            claims?.hasRole("PATIENT") == true

    // =====================================================
    // NAVIGATION OPTIONS
    // =====================================================

    val items = buildList {

        // INICIO: disponible para todos.
        add(
            BottomNavigationItem(
                route = Routes.HOME,
                title = "Inicio",
                icon = Icons.Outlined.Home
            )
        )

        when {

            // =============================================
            // ADMINISTRATOR
            // =============================================

            isAdmin -> {

                add(
                    BottomNavigationItem(
                        route = Routes.APPOINTMENTS,
                        title = "Citas",
                        icon = Icons.Outlined.CalendarMonth
                    )
                )

                add(
                    BottomNavigationItem(
                        route = Routes.APPOINTMENT_CALENDAR,
                        title = "Calendario",
                        icon = Icons.Outlined.DateRange
                    )
                )

                add(
                    BottomNavigationItem(
                        route = Routes.PATIENTS,
                        title = "Pacientes",
                        icon = Icons.Outlined.People
                    )
                )
            }

            // =============================================
            // DOCTOR
            // =============================================

            isDoctor -> {

                add(
                    BottomNavigationItem(
                        route = Routes.APPOINTMENTS,
                        title = "Mi agenda",
                        icon = Icons.Outlined.CalendarMonth
                    )
                )
            }

            // =============================================
            // PATIENT
            // =============================================

            isPatient -> {

                add(
                    BottomNavigationItem(
                        route = Routes.APPOINTMENTS,
                        title = "Mis citas",
                        icon = Icons.Outlined.CalendarMonth
                    )
                )

                add(
                    BottomNavigationItem(
                        route = Routes.APPOINTMENT_CALENDAR,
                        title = "Calendario",
                        icon = Icons.Outlined.DateRange
                    )
                )
            }
        }
    }

    // =====================================================
    // COLORS
    // =====================================================

    val colors = MaterialTheme.colorScheme

    val navigationColors =
        NavigationBarItemDefaults.colors(

            selectedIconColor = colors.primary,

            selectedTextColor = colors.primary,

            indicatorColor = colors.primaryContainer,

            unselectedIconColor =
                colors.onSurfaceVariant,

            unselectedTextColor =
                colors.onSurfaceVariant
        )

    // =====================================================
    // BOTTOM NAVIGATION
    // =====================================================

    NavigationBar(
        modifier = modifier,
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp
    ) {

        items.forEach { item ->

            val isSelected =
                currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,

                onClick = {

                    /*
                     * Evita solicitar navegación
                     * cuando ya estamos en esa ruta.
                     */
                    if (!isSelected) {
                        onNavigate(item.route)
                    }
                },

                icon = {

                    Icon(
                        imageVector = item.icon,

                        contentDescription = null,

                        modifier = Modifier.size(
                            24.dp
                        )
                    )
                },

                label = {

                    Text(
                        text = item.title,

                        style =
                            MaterialTheme.typography.bodySmall,

                        fontWeight =
                            if (isSelected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Medium
                            },

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                },

                alwaysShowLabel = true,

                colors = navigationColors
            )
        }
    }
}
