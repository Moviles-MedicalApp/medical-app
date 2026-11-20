package pe.com.smart.presentation.appointments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.domain.model.Appointment

/**
 * Pantalla de citas del paciente.
 *
 * Funcionalidades:
 * - Consultar citas del paciente autenticado.
 * - Separar próximas citas e historial.
 * - Reservar una nueva cita mediante navegación.
 * - Cancelar citas activas con confirmación.
 *
 * No permite confirmar ni completar atenciones.
 */
@Composable
fun PatientAppointmentsScreen(
    onBookAppointment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppointmentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var appointmentToCancel by remember {
        mutableStateOf<Appointment?>(null)
    }

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    val today = remember {
        val instant = Instant.fromEpochMilliseconds(
            kotlin.time.Clock.System.now()
                .toEpochMilliseconds()
        )

        instant.toLocalDateTime(
            TimeZone.currentSystemDefault()
        ).date.toString()
    }

    val orderedAppointments = state.appointments
        .sortedWith(
            compareBy<Appointment> {
                it.appointmentDate
            }.thenBy {
                it.startTime
            }
        )

    val upcomingAppointments = orderedAppointments.filter {
        it.status in setOf("SCHEDULED", "CONFIRMED") &&
                it.appointmentDate >= today
    }

    val historyAppointments = orderedAppointments.filter {
        it.status in setOf("COMPLETED", "CANCELLED") ||
                (
                        it.status in setOf("SCHEDULED", "CONFIRMED") &&
                                it.appointmentDate < today
                        )
    }.reversed()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading && state.appointments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null &&
                    state.appointments.isEmpty() -> {
                PatientAgendaError(
                    message = state.error
                        ?: "No se pudieron cargar tus citas.",
                    onRetry = {
                        viewModel.load()
                    }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 18.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PatientAgendaHeader(
                            upcomingCount =
                                upcomingAppointments.size,
                            onBookAppointment =
                                onBookAppointment
                        )
                    }

                    item {
                        PatientAgendaTabs(
                            selectedTab = selectedTab,
                            upcomingCount =
                                upcomingAppointments.size,
                            historyCount =
                                historyAppointments.size,
                            onTabSelected = {
                                selectedTab = it
                            }
                        )
                    }

                    if (state.isLoading) {
                        item {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )

                                Text(
                                    text = "Actualizando citas...",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (state.error != null) {
                        item {
                            Surface(
                                color = MaterialTheme
                                    .colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.error.orEmpty(),
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    val visibleAppointments =
                        if (selectedTab == 0) {
                            upcomingAppointments
                        } else {
                            historyAppointments
                        }

                    if (visibleAppointments.isEmpty()) {
                        item {
                            PatientAgendaEmpty(
                                isHistory = selectedTab == 1,
                                onBookAppointment =
                                    onBookAppointment
                            )
                        }
                    } else {
                        items(
                            items = visibleAppointments,
                            key = { it.id }
                        ) { appointment ->
                            PatientAgendaAppointmentCard(
                                appointment = appointment,
                                canCancel =
                                    selectedTab == 0 &&
                                            appointment.status in
                                            setOf(
                                                "SCHEDULED",
                                                "CONFIRMED"
                                            ),
                                isSaving = state.isSaving,
                                onCancel = {
                                    appointmentToCancel =
                                        appointment
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    AppConfirmDialog(
        visible = appointmentToCancel != null,
        title = "Cancelar cita",
        message = buildString {
            append("¿Deseas cancelar tu cita")
            appointmentToCancel?.let { appointment ->
                append(" del ")
                append(
                    formatPatientAppointmentDate(
                        appointment.appointmentDate
                    )
                )
                append(" a las ")
                append(appointment.startTime.take(5))
            }
            append("? Esta acción podría no ser reversible.")
        },
        confirmText = "Cancelar cita",
        cancelText = "Volver",
        loadingText = "Cancelando...",
        icon = Icons.Outlined.Cancel,
        destructive = true,
        isLoading = state.isSaving,
        onConfirm = {
            if (!state.isSaving) {
                appointmentToCancel?.let { appointment ->
                    viewModel.changeStatus(
                        appointment = appointment,
                        action = "cancel"
                    )
                }
                appointmentToCancel = null
            }
        },
        onDismiss = {
            if (!state.isSaving) {
                appointmentToCancel = null
            }
        }
    )
}

/**
 * Encabezado con resumen y acción principal.
 */
@Composable
private fun PatientAgendaHeader(
    upcomingCount: Int,
    onBookAppointment: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Mis citas médicas",
                style = MaterialTheme
                    .typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Text(
                text = "Consulta tus próximas atenciones y revisa tu historial.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = upcomingCount.toString(),
                        style = MaterialTheme
                            .typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer
                    )

                    Text(
                        text = if (upcomingCount == 1) {
                            "Próxima cita"
                        } else {
                            "Próximas citas"
                        },
                        style = MaterialTheme
                            .typography.bodyMedium,
                        color = colors.onPrimaryContainer
                    )
                }
            }
        }

        Button(
            onClick = onBookAppointment,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(
                modifier = Modifier.size(8.dp)
            )

            Text(
                text = "Reservar nueva cita",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Pestañas para próximas citas e historial.
 */
@Composable
private fun PatientAgendaTabs(
    selectedTab: Int,
    upcomingCount: Int,
    historyCount: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
            },
            text = {
                Text(
                    text = "Próximas ($upcomingCount)",
                    fontWeight = if (selectedTab == 0) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            },
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription = null
                )
            }
        )

        Tab(
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            },
            text = {
                Text(
                    text = "Historial ($historyCount)",
                    fontWeight = if (selectedTab == 1) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null
                )
            }
        )
    }
}

/**
 * Tarjeta individual de una cita del paciente.
 */
@Composable
private fun PatientAgendaAppointmentCard(
    appointment: Appointment,
    canCancel: Boolean,
    isSaving: Boolean,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.CalendarMonth,
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
                        text = formatPatientAppointmentDate(
                            appointment.appointmentDate
                        ),
                        style = MaterialTheme
                            .typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = appointment.timeRange,
                            style = MaterialTheme
                                .typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                PatientAgendaInfoRow(
                    icon = Icons.Outlined.PersonOutline,
                    label = "Médico",
                    value = appointment.doctorFullName
                )

                PatientAgendaInfoRow(
                    icon = Icons.Outlined.LocalHospital,
                    label = "Especialidad",
                    value = appointment.specialityName
                )

                if (appointment.reason.isNotBlank()) {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Motivo de consulta",
                            style = MaterialTheme
                                .typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurfaceVariant
                        )

                        Text(
                            text = appointment.reason,
                            style = MaterialTheme
                                .typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }

            PatientAgendaStatusBadge(
                status = appointment.status
            )

            if (canCancel) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSaving) {
                            colors.onSurfaceVariant
                        } else {
                            colors.error
                        }
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text(
                        text = "Cancelar cita",
                        color = if (isSaving) {
                            colors.onSurfaceVariant
                        } else {
                            colors.error
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Fila de información reutilizable.
 */
@Composable
private fun PatientAgendaInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            Text(
                text = value.ifBlank {
                    "No especificado"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Estado visual de la cita.
 */
@Composable
private fun PatientAgendaStatusBadge(
    status: String
) {
    val colors = MaterialTheme.colorScheme

    val label: String
    val background: Color
    val foreground: Color

    when (status.uppercase()) {
        "SCHEDULED" -> {
            label = "Programada"
            background = colors.secondaryContainer
            foreground = colors.onSecondaryContainer
        }

        "CONFIRMED" -> {
            label = "Confirmada"
            background = colors.primaryContainer
            foreground = colors.onPrimaryContainer
        }

        "COMPLETED" -> {
            label = "Completada"
            background = Color(0xFFE0F6ED)
            foreground = Color(0xFF167347)
        }

        "CANCELLED" -> {
            label = "Cancelada"
            background = colors.errorContainer
            foreground = colors.onErrorContainer
        }

        else -> {
            label = status
            background = colors.surfaceVariant
            foreground = colors.onSurfaceVariant
        }
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = background
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = foreground
        )
    }
}

/**
 * Estado vacío.
 */
@Composable
private fun PatientAgendaEmpty(
    isHistory: Boolean,
    onBookAppointment: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isHistory) {
                Icons.Outlined.History
            } else {
                Icons.Outlined.EventAvailable
            },
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(44.dp)
        )

        Text(
            text = if (isHistory) {
                "Tu historial está vacío"
            } else {
                "No tienes próximas citas"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isHistory) {
                "Tus atenciones anteriores aparecerán aquí."
            } else {
                "Reserva una consulta para comenzar."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        if (!isHistory) {
            OutlinedButton(
                onClick = onBookAppointment
            ) {
                Text("Reservar cita")
            }
        }
    }
}

/**
 * Estado de error con reintento.
 */
@Composable
private fun PatientAgendaError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Cancel,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(42.dp)
        )

        Spacer(
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = "No se pudieron cargar las citas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.size(16.dp)
        )

        Button(
            onClick = onRetry
        ) {
            Text("Reintentar")
        }
    }
}

/**
 * Convierte fechas ISO al formato español.
 *
 * 2026-11-18 -> 18 de noviembre de 2026
 */
private fun formatPatientAppointmentDate(
    value: String
): String {
    val parts = value.split("-")

    if (parts.size != 3) {
        return value
    }

    val year = parts[0].toIntOrNull()
        ?: return value

    val month = parts[1].toIntOrNull()
        ?: return value

    val day = parts[2].toIntOrNull()
        ?: return value

    val months = listOf(
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "septiembre",
        "octubre",
        "noviembre",
        "diciembre"
    )

    if (month !in 1..12 || day !in 1..31) {
        return value
    }

    return "$day de ${months[month - 1]} de $year"
}
