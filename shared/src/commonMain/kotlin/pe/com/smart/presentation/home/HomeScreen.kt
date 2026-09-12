package pe.com.smart.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize(),

        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 32.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {

        /*
         * =================================================
         * SALUDO
         * =================================================
         */
        item {

            HomeGreeting(
                username = "admin"
            )
        }

        /*
         * =================================================
         * RESUMEN
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title = "Resumen"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    SummaryCard(
                        title = "Citas hoy",
                        value = "0",
                        icon = Icons.Outlined.CalendarMonth,
                        modifier = Modifier.weight(1f),
                        onClick = onAppointmentsClick
                    )

                    SummaryCard(
                        title = "Pacientes",
                        value = "4",
                        icon = Icons.Outlined.People,
                        modifier = Modifier.weight(1f),
                        onClick = onPatientsClick
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    SummaryCard(
                        title = "Médicos",
                        value = "3",
                        icon = Icons.Outlined.MedicalServices,
                        modifier = Modifier.weight(1f),
                        onClick = onDoctorsClick
                    )

                    SummaryCard(
                        title = "Especialidades",
                        value = "4",
                        icon = Icons.Outlined.LocalHospital,
                        modifier = Modifier.weight(1f),
                        onClick = onSpecialitiesClick
                    )
                }
            }
        }

        /*
         * =================================================
         * AGENDA DE HOY
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title = "Agenda de hoy"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                TodayAgendaCard(
                    onClick =
                        onAppointmentsClick
                )
            }
        }

        /*
         * =================================================
         * PRÓXIMA CITA
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title = "Próxima cita"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                NextAppointmentCard(
                    onClick =
                        onAppointmentsClick
                )
            }
        }

        /*
         * =================================================
         * ACCIONES RÁPIDAS
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title = "Acciones rápidas"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                QuickActions(
                    onNewAppointmentClick =
                        onNewAppointmentClick,

                    onCalendarClick =
                        onCalendarClick
                )
            }
        }
    }
}


/*
 * =====================================================
 * SALUDO
 * =====================================================
 */

@Composable
private fun HomeGreeting(
    username: String
) {

    Column {

        Text(
            text =
                "Buenos días, $username",

            style =
                MaterialTheme.typography.headlineSmall,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Text(
            text =
                "Este es el resumen de hoy",

            style =
                MaterialTheme.typography.bodyLarge,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/*
 * =====================================================
 * TÍTULO DE SECCIÓN
 * =====================================================
 */

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text =
            title,

        style =
            MaterialTheme.typography.headlineSmall,

        fontWeight =
            FontWeight.Bold,

        color =
            MaterialTheme.colorScheme.onBackground
    )
}


/*
 * =====================================================
 * TARJETA DE RESUMEN
 * =====================================================
 */

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .height(112.dp)
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        title,

                    style =
                        MaterialTheme.typography.titleSmall,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis,

                    modifier =
                        Modifier.weight(1f)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(27.dp),

                    tint =
                        MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text =
                    value,

                style =
                    MaterialTheme.typography.headlineMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


/*
 * =====================================================
 * AGENDA DEL DÍA
 * =====================================================
 */

@Composable
private fun TodayAgendaCard(
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(
                modifier =
                    Modifier.size(52.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.CalendarMonth,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(28.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Text(
                text =
                    "Sin citas para hoy",

                style =
                    MaterialTheme.typography.titleMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "La agenda del día está libre",

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/*
 * =====================================================
 * PRÓXIMA CITA
 * =====================================================
 */

@Composable
private fun NextAppointmentCard(
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier =
                    Modifier.size(52.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.EventAvailable,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(27.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "No hay próximas citas",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "No hay atenciones pendientes registradas.",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/*
 * =====================================================
 * ACCIONES RÁPIDAS
 * =====================================================
 */

@Composable
private fun QuickActions(
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        QuickActionCard(
            title =
                "Nueva cita",

            subtitle =
                "Registrar atención",

            icon =
                Icons.Outlined.Add,

            onClick =
                onNewAppointmentClick,

            modifier =
                Modifier.weight(1f)
        )

        QuickActionCard(
            title =
                "Agenda",

            subtitle =
                "Ver horarios",

            icon =
                Icons.Outlined.Schedule,

            onClick =
                onCalendarClick,

            modifier =
                Modifier.weight(1f)
        )
    }
}


/*
 * =====================================================
 * TARJETA DE ACCIÓN RÁPIDA
 * =====================================================
 */

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme.outlineVariant
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {

            Surface(
                modifier =
                    Modifier.size(42.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            icon,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(22.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    title,

                style =
                    MaterialTheme.typography.titleSmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text =
                    subtitle,

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}