
package pe.com.smart.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.presentation.home.components.NextAppointmentCard
import pe.com.smart.presentation.session.SessionIdentityStore

@Composable
fun HomeScreen(
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val identity by SessionIdentityStore.uiState
        .collectAsStateWithLifecycle()

    val token = TokenStorage.getToken().orEmpty()
    val claims = AccessTokenClaimsDecoder.decode(token)

    LaunchedEffect(token) {
        SessionIdentityStore.ensureLoaded()
    }

    val displayFirstName = when (state.role) {
        HomeRole.DOCTOR,
        HomeRole.PATIENT -> {
            identity.profile?.firstName
                ?.takeIf { it.isNotBlank() }
                ?: claims?.displayFirstName
                ?: state.username
        }

        else -> {
            claims?.displayFirstName
                ?: state.username
        }
    }.ifBlank { "Usuario" }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Solo mostrar carga completa durante
            // la primera consulta.
            state.isLoading &&
                    state.role == HomeRole.UNKNOWN -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            state.error != null &&
                    state.role == HomeRole.UNKNOWN -> {
                DashboardError(
                    message = state.error
                        ?: "No se pudo cargar el resumen.",
                    onRetry = {
                        viewModel.retry()
                    },
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.error != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = MaterialTheme
                                .colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = state.error.orEmpty(),
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme
                                        .colorScheme.onErrorContainer,
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )

                                OutlinedButton(
                                    onClick = {
                                        viewModel.retry()
                                    }
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }

                    when (state.role) {
                        HomeRole.DOCTOR -> {
                            DoctorHomeContent(
                                state = state,
                                firstName = displayFirstName,
                                onAppointmentsClick =
                                    onAppointmentsClick,
                                onRefresh = {
                                    viewModel.retry()
                                }
                            )
                        }

                        HomeRole.PATIENT -> {
                            PatientHomeContent(
                                state = state,
                                firstName = displayFirstName,
                                onAppointmentsClick =
                                    onAppointmentsClick,
                                onNewAppointmentClick =
                                    onNewAppointmentClick,
                                onCalendarClick =
                                    onCalendarClick,
                                onRefresh = {
                                    viewModel.retry()
                                }
                            )
                        }

                        HomeRole.ADMIN -> {
                            AdminHomeContent(
                                state = state,
                                firstName = displayFirstName,
                                onAppointmentsClick =
                                    onAppointmentsClick,
                                onPatientsClick =
                                    onPatientsClick,
                                onDoctorsClick =
                                    onDoctorsClick,
                                onSpecialitiesClick =
                                    onSpecialitiesClick,
                                onNewAppointmentClick =
                                    onNewAppointmentClick,
                                onCalendarClick =
                                    onCalendarClick,
                                onRefresh = {
                                    viewModel.retry()
                                }
                            )
                        }

                        HomeRole.UNKNOWN -> {
                            DashboardError(
                                message =
                                    "No se pudo identificar el rol del usuario.",
                                onRetry = {
                                    viewModel.retry()
                                },
                                modifier = Modifier.align(
                                    Alignment.CenterHorizontally
                                )
                            )
                        }
                    }
                }
            }
        }

        if (
            state.isLoading &&
            state.role != HomeRole.UNKNOWN
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(22.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

// =====================================================
// MÉDICO
// =====================================================

@Composable
private fun DoctorHomeContent(
    state: HomeUiState,
    firstName: String,
    onAppointmentsClick: () -> Unit,
    onRefresh: () -> Unit
) {
    DashboardList {
        item {
            DashboardGreeting(
                firstName = firstName,
                description = "Consulta tu agenda clínica.",
                onRefresh = onRefresh
            )
        }

        item {
            SectionHeader(
                title = "Resumen de jornada",
                subtitle = "Atenciones confirmadas"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Confirmadas hoy",
                    value = state.todayAppointments.toString(),
                    icon = Icons.Outlined.EventAvailable,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )

                DashboardMetricCard(
                    title = "Próximas",
                    value =
                        state.upcomingAppointments.toString(),
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )
            }
        }

        item {
            SectionHeader(
                title = "Próxima atención",
                subtitle =
                    "Tu siguiente consulta confirmada"
            )
        }

        item {
            NextAppointmentCard(
                appointment = state.nextAppointment,
                isDoctor = true,
                onClick = onAppointmentsClick
            )
        }

        item {
            DashboardActionCard(
                title = "Mi agenda",
                description =
                    "Consultar mis citas confirmadas",
                icon = Icons.Outlined.CalendarMonth,
                onClick = onAppointmentsClick
            )
        }
    }
}

// =====================================================
// PACIENTE
// =====================================================

@Composable
private fun PatientHomeContent(
    state: HomeUiState,
    firstName: String,
    onAppointmentsClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onRefresh: () -> Unit
) {
    DashboardList {
        item {
            DashboardGreeting(
                firstName = firstName,
                description =
                    "Gestiona tus consultas médicas.",
                onRefresh = onRefresh
            )
        }

        item {
            SectionHeader(
                title = "Mis citas",
                subtitle = "Resumen de tus atenciones"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Citas de hoy",
                    value = state.todayAppointments.toString(),
                    icon = Icons.Outlined.EventAvailable,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )

                DashboardMetricCard(
                    title = "Próximas",
                    value =
                        state.upcomingAppointments.toString(),
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )
            }
        }

        item {
            SectionHeader(
                title = "Próxima consulta"
            )
        }

        item {
            NextAppointmentCard(
                appointment = state.nextAppointment,
                isDoctor = false,
                onClick = onAppointmentsClick
            )
        }

        item {
            Button(
                onClick = onNewAppointmentClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )

                Text(
                    text = "Reservar nueva cita",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        item {
            SectionHeader(
                title = "Accesos rápidos"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardQuickAction(
                    title = "Mis citas",
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )

                DashboardQuickAction(
                    title = "Calendario",
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = onCalendarClick
                )
            }
        }
    }
}

// =====================================================
// ADMINISTRADOR
// =====================================================

@Composable
private fun AdminHomeContent(
    state: HomeUiState,
    firstName: String,
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onRefresh: () -> Unit
) {
    DashboardList {
        item {
            DashboardGreeting(
                firstName = firstName,
                description = "Resumen operativo de la clínica.",
                onRefresh = onRefresh
            )
        }

        if (
            state.unconfirmedAppointments > 0 ||
            state.doctorsWithoutSchedules > 0
        ) {
            item {
                SectionHeader(
                    title = "Requiere atención",
                    subtitle = "Asuntos pendientes de gestión"
                )
            }

            if (state.unconfirmedAppointments > 0) {
                item {
                    DashboardAlertCard(
                        title = "Citas por confirmar",
                        description =
                            "${state.unconfirmedAppointments} citas requieren revisión.",
                        onClick = onAppointmentsClick
                    )
                }
            }

            if (state.doctorsWithoutSchedules > 0) {
                item {
                    DashboardAlertCard(
                        title = "Médicos sin horario",
                        description =
                            "${state.doctorsWithoutSchedules} médicos no tienen horarios configurados.",
                        onClick = onDoctorsClick
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Resumen operativo",
                subtitle = "Indicadores generales"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Citas de hoy",
                    value = state.todayAppointments.toString(),
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onAppointmentsClick
                )

                DashboardMetricCard(
                    title = "Pacientes",
                    value = state.patients.toString(),
                    icon = Icons.Outlined.People,
                    modifier = Modifier.weight(1f),
                    onClick = onPatientsClick
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardMetricCard(
                    title = "Médicos",
                    value = state.doctors.toString(),
                    icon = Icons.Outlined.MedicalServices,
                    modifier = Modifier.weight(1f),
                    onClick = onDoctorsClick
                )

                DashboardMetricCard(
                    title = "Especialidades",
                    value = state.specialities.toString(),
                    icon = Icons.Outlined.People,
                    modifier = Modifier.weight(1f),
                    onClick = onSpecialitiesClick
                )
            }
        }

        item {
            SectionHeader(
                title = "Próxima cita",
                subtitle = "Siguiente atención programada"
            )
        }

        item {
            NextAppointmentCard(
                appointment = state.nextAppointment,
                onClick = onAppointmentsClick
            )
        }

        item {
            SectionHeader(
                title = "Accesos rápidos"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                DashboardQuickAction(
                    title = "Nueva cita",
                    icon = Icons.Outlined.Add,
                    modifier = Modifier.weight(1f),
                    onClick = onNewAppointmentClick
                )

                DashboardQuickAction(
                    title = "Calendario",
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onCalendarClick
                )
            }
        }

        item {
            DashboardActionCard(
                title = "Gestionar pacientes",
                description =
                    "Consultar los pacientes registrados",
                icon = Icons.Outlined.People,
                onClick = onPatientsClick
            )
        }
    }
}

// =====================================================
// COMPONENTES DEL DASHBOARD
// =====================================================

@Composable
private fun DashboardList(
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
private fun DashboardGreeting(
    firstName: String,
    description: String,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Hola, $firstName",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Actualizar resumen",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme
                    .typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = colors.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardQuickAction(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(26.dp)
            )

            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashboardAlertCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EventAvailable,
                contentDescription = null,
                tint = colors.onErrorContainer
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme
                        .typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onErrorContainer
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onErrorContainer
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.onErrorContainer
            )
        }
    }
}

@Composable
private fun DashboardError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No se pudo cargar el inicio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onRetry
        ) {
            Text("Reintentar")
        }
    }
}
