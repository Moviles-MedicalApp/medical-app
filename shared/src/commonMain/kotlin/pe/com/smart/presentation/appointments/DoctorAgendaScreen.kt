
package pe.com.smart.presentation.appointments

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventAvailable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.domain.model.Appointment
import pe.com.smart.presentation.appointments.components.AppointmentStatusBadge

/**
 * =========================================================
 * DOCTOR AGENDA SCREEN
 * =========================================================
 *
 * Pantalla exclusiva del rol DOCTOR.
 *
 * Funcionalidades:
 * - Consultar citas confirmadas.
 * - Ver información del paciente.
 * - Consultar fecha, horario y especialidad.
 * - Consultar motivo de atención.
 * - Completar una cita confirmada.
 *
 * No permite crear, editar ni cancelar citas.
 */
@Composable
fun DoctorAgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AppointmentsViewModel = viewModel()
) {

    // =====================================================
    // UI STATE
    // =====================================================

    val state by viewModel.state
        .collectAsStateWithLifecycle()

    var completing by remember {
        mutableStateOf<Appointment?>(null)
    }

    // =====================================================
    // INITIAL LOAD
    // =====================================================

    LaunchedEffect(viewModel) {
        viewModel.load(
            doctorAgendaStatus = "CONFIRMED"
        )
    }

    // =====================================================
    // SORTED APPOINTMENTS
    // =====================================================

    val appointments = state.appointments
        .filter {
            it.status == "CONFIRMED"
        }
        .sortedWith(
            compareBy<Appointment> {
                it.appointmentDate
            }.thenBy {
                it.startTime
            }
        )

    // =====================================================
    // SCREEN CONTENT
    // =====================================================

    when {

        // =============================================
        // LOADING
        // =============================================

        state.isLoading &&
                state.appointments.isEmpty() -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // =============================================
        // ERROR
        // =============================================

        state.error != null &&
                state.appointments.isEmpty() -> {

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppError(
                    message = state.error
                        ?: "No se pudo cargar tu agenda.",
                    onRetry = {
                        viewModel.load(
                            doctorAgendaStatus = "CONFIRMED"
                        )
                    }
                )
            }
        }

        // =============================================
        // CONTENT
        // =============================================

        else -> {

            LazyColumn(
                modifier = modifier.fillMaxSize(),

                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 20.dp,
                    end = 16.dp,
                    bottom = 32.dp
                ),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                // =====================================
                // HEADER
                // =====================================

                item {
                    DoctorAgendaHeader(
                        appointmentCount =
                            appointments.size
                    )
                }

                // =====================================
                // REFRESH STATUS
                // =====================================

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
                                text = "Actualizando agenda...",
                                style =
                                    MaterialTheme.typography.bodySmall,

                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }

                // =====================================
                // EMPTY STATE
                // =====================================

                if (appointments.isEmpty()) {

                    item {
                        AppEmptyState(
                            title =
                                "No tienes citas confirmadas",

                            message =
                                "Las citas confirmadas y asignadas a ti aparecerán aquí."
                        )
                    }

                } else {

                    // =================================
                    // APPOINTMENT LIST
                    // =================================

                    items(
                        items = appointments,
                        key = { appointment ->
                            appointment.id
                        }
                    ) { appointment ->

                        DoctorAgendaAppointmentCard(
                            appointment = appointment,

                            isSaving = state.isSaving,

                            onComplete = {
                                completing = appointment
                            }
                        )
                    }
                }
            }
        }
    }

    // =====================================================
    // COMPLETE APPOINTMENT CONFIRMATION
    // =====================================================

    AppConfirmDialog(
        visible = completing != null,

        title = "Completar cita",

        message = buildString {
            append("¿Deseas marcar como completada ")
            append("la atención de ")
            append(
                completing?.patientFullName
                    ?: "este paciente"
            )
            append("?")
        },

        confirmText = "Completar cita",

        icon = Icons.Outlined.CheckCircle,

        isLoading = state.isSaving,

        onConfirm = {

            if (!state.isSaving) {

                completing?.let { appointment ->
                    viewModel.changeStatus(
                        appointment = appointment,
                        action = "complete"
                    )
                }

                completing = null
            }
        },

        onDismiss = {
            if (!state.isSaving) {
                completing = null
            }
        }
    )
}

// =========================================================
// AGENDA HEADER
// =========================================================

@Composable
private fun DoctorAgendaHeader(
    appointmentCount: Int
) {

    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Column(
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = "Citas confirmadas",

                style =
                    MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold,

                color = colors.onBackground
            )

            Text(
                text =
                    "Consulta y gestiona tus atenciones asignadas.",

                style =
                    MaterialTheme.typography.bodyMedium,

                color = colors.onSurfaceVariant
            )
        }

        // =============================================
        // SUMMARY CARD
        // =============================================

        Card(
            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(18.dp),

            colors = CardDefaults.cardColors(
                containerColor = colors.primaryContainer
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                Surface(
                    modifier = Modifier.size(48.dp),

                    shape = RoundedCornerShape(14.dp),

                    color = colors.surface
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.EventAvailable,

                            contentDescription = null,

                            tint = colors.primary,

                            modifier =
                                Modifier.size(26.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = appointmentCount.toString(),

                        style =
                            MaterialTheme.typography
                                .headlineMedium,

                        fontWeight = FontWeight.Bold,

                        color =
                            colors.onPrimaryContainer
                    )

                    Text(
                        text = if (appointmentCount == 1) {
                            "Cita confirmada"
                        } else {
                            "Citas confirmadas"
                        },

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            colors.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// =========================================================
// APPOINTMENT CARD
// =========================================================

@Composable
private fun DoctorAgendaAppointmentCard(
    appointment: Appointment,
    isSaving: Boolean,
    onComplete: () -> Unit
) {

    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),

        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
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

            // =============================================
            // DATE AND TIME
            // =============================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Surface(
                    modifier = Modifier.size(44.dp),

                    shape = RoundedCornerShape(12.dp),

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

                            modifier =
                                Modifier.size(23.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {

                    Text(
                        text = formatDoctorAgendaDate(
                            appointment.appointmentDate
                        ),

                        style =
                            MaterialTheme.typography.titleSmall,

                        fontWeight = FontWeight.SemiBold,

                        color = colors.onSurface
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Schedule,

                            contentDescription = null,

                            modifier =
                                Modifier.size(16.dp),

                            tint = colors.primary
                        )

                        Text(
                            text = appointment.timeRange,

                            style =
                                MaterialTheme.typography.bodySmall,

                            color = colors.primary,

                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            // =============================================
            // PATIENT
            // =============================================

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "PACIENTE",

                    style =
                        MaterialTheme.typography.labelLarge,

                    fontWeight = FontWeight.SemiBold,

                    color = colors.onSurfaceVariant
                )

                Text(
                    text = appointment.patientFullName
                        .ifBlank {
                            "Paciente no especificado"
                        },

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.Bold,

                    color = colors.onSurface,

                    maxLines = 2,

                    overflow = TextOverflow.Ellipsis
                )

                if (appointment.patientDni.isNotBlank()) {

                    DoctorAgendaInfoRow(
                        icon = Icons.Outlined.PersonOutline,

                        text =
                            "DNI: ${appointment.patientDni}"
                    )
                }

                if (appointment.specialityName.isNotBlank()) {

                    DoctorAgendaInfoRow(
                        icon =
                            Icons.Outlined.LocalHospital,

                        text =
                            appointment.specialityName
                    )
                }
            }

            // =============================================
            // REASON
            // =============================================

            if (appointment.reason.isNotBlank()) {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {

                    Text(
                        text = "Motivo de consulta",

                        style =
                            MaterialTheme.typography.labelLarge,

                        fontWeight =
                            FontWeight.SemiBold,

                        color = colors.onSurfaceVariant
                    )

                    Text(
                        text = appointment.reason,

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color = colors.onSurfaceVariant
                    )
                }
            }

            // =============================================
            // STATUS
            // =============================================

            AppointmentStatusBadge(
                appointment.status
            )

            // =============================================
            // COMPLETE ACTION
            // =============================================

            Button(
                onClick = onComplete,

                enabled = !isSaving,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.CheckCircle,

                    contentDescription = null,

                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Completar atención",

                    modifier = Modifier.padding(
                        start = 8.dp
                    )
                )
            }
        }
    }
}

// =========================================================
// INFORMATION ROW
// =========================================================

@Composable
private fun DoctorAgendaInfoRow(
    icon: ImageVector,
    text: String
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        Icon(
            imageVector = icon,

            contentDescription = null,

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant,

            modifier = Modifier.size(18.dp)
        )

        Text(
            text = text,

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,

            maxLines = 2,

            overflow = TextOverflow.Ellipsis
        )
    }
}

// =========================================================
// DATE FORMAT
// =========================================================

/**
 * Convierte una fecha ISO a formato legible.
 *
 * Entrada:
 * 2026-11-18
 *
 * Salida:
 * 18 de noviembre de 2026
 *
 * No requiere dependencias adicionales.
 */
private fun formatDoctorAgendaDate(
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
