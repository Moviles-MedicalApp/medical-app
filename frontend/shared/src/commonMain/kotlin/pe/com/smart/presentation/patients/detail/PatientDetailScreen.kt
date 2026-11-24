
package pe.com.smart.presentation.patients.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.koin.compose.koinInject
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.model.Patient
import pe.com.smart.domain.usecase.AppointmentUseCase
import pe.com.smart.domain.usecase.PatientUseCase
import pe.com.smart.presentation.appointments.components.AppointmentStatusBadge

/**
 * Pantalla de detalle de paciente.
 *
 * Mantiene:
 * - Carga por ID.
 * - Actualización mediante refreshKey.
 * - Edición del paciente.
 * - Creación de una cita.
 * - Eliminación con confirmación.
 */
@Composable
fun PatientDetailScreen(
    patientId: Int,
    refreshKey: Int = 0,
    onEditClick: (Int) -> Unit,
    onNewAppointmentClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val patientUseCase: PatientUseCase = koinInject()
    val appointmentUseCase: AppointmentUseCase = koinInject()
    val viewModel: PatientDetailViewModel = viewModel(
        key = "patient_detail_$patientId"
    ) {
        PatientDetailViewModel(patientId, patientUseCase, appointmentUseCase)
    }

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        patientId,
        refreshKey,
        viewModel
    ) {
        viewModel.loadPatient()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.patient == null -> {
                AppLoading(
                    message = "Cargando paciente..."
                )
            }

            uiState.error != null &&
                    uiState.patient == null -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar el paciente.",
                    onRetry = viewModel::loadPatient
                )
            }

            uiState.patient != null -> {
                val patient = uiState.patient!!

                PatientDetailContent(
                    patient = patient,
                    upcomingAppointments =
                        uiState.upcomingAppointments,
                    appointmentHistory =
                        uiState.appointmentHistory,
                    isDeleting = uiState.isDeleting,
                    error = uiState.error,
                    onEditClick = {
                        onEditClick(patient.id)
                    },
                    onNewAppointmentClick = {
                        onNewAppointmentClick(patient.id)
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    AppConfirmDialog(
        visible = showDeleteDialog,
        title = "Eliminar paciente",
        message =
            "¿Estás seguro de que deseas eliminar este paciente? Esta acción no se puede deshacer.",
        confirmText = "Eliminar",
        cancelText = "Cancelar",
        loadingText = "Eliminando...",
        icon = Icons.Outlined.Delete,
        destructive = true,
        isLoading = uiState.isDeleting,
        onConfirm = {
            if (!uiState.isDeleting) {
                viewModel.deletePatient(
                    onSuccess = {
                        showDeleteDialog = false
                        onDeleted()
                    }
                )
            }
        },
        onDismiss = {
            if (!uiState.isDeleting) {
                showDeleteDialog = false
            }
        }
    )
}

/**
 * Contenido principal del detalle.
 */
@Composable
private fun PatientDetailContent(
    patient: Patient,
    upcomingAppointments: List<Appointment>,
    appointmentHistory: List<Appointment>,
    isDeleting: Boolean,
    error: String?,
    onEditClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 20.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {
        PatientProfileCard(patient)

        if (!error.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme
                    .colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme
                        .colorScheme.onErrorContainer
                )
            }
        }

        PatientContactCard(patient)

        PatientAppointmentsSection(
            upcomingAppointments = upcomingAppointments,
            appointmentHistory = appointmentHistory,
            onNewAppointment = onNewAppointmentClick
        )

        PatientActionsSection(
            isDeleting = isDeleting,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )

        Spacer(Modifier.size(12.dp))
    }
}

/**
 * Identidad del paciente.
 */
@Composable
private fun PatientProfileCard(
    patient: Patient
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
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = patientInitials(
                                patient.fullName
                            ),
                            style = MaterialTheme
                                .typography.titleLarge,
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
                        text = patient.fullName,
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.onSurface
                    )

                    Text(
                        text = "Paciente registrado",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            PatientInformationRow(
                icon = Icons.Outlined.Badge,
                label = "Documento de identidad",
                value = "DNI: ${patient.dni}"
            )
        }
    }
}

/**
 * Información de contacto.
 */
@Composable
private fun PatientContactCard(
    patient: Patient
) {
    SectionCard(
        title = "Información de contacto"
    ) {
        PatientInformationRow(
            icon = Icons.Outlined.Email,
            label = "Correo electrónico",
            value = patient.email
        )

        HorizontalDivider(
            color = MaterialTheme
                .colorScheme.outlineVariant
        )

        PatientInformationRow(
            icon = Icons.Outlined.Phone,
            label = "Teléfono",
            value = patient.phone
        )
    }
}

/**
 * Resumen y listado de citas del paciente.
 */
@Composable
private fun PatientAppointmentsSection(
    upcomingAppointments: List<Appointment>,
    appointmentHistory: List<Appointment>,
    onNewAppointment: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Citas médicas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            PatientStatCard(
                count = upcomingAppointments.size,
                label = "Activas",
                icon = Icons.Outlined.EventAvailable,
                modifier = Modifier.weight(1f)
            )

            PatientStatCard(
                count = appointmentHistory.size,
                label = "Historial",
                icon = Icons.Outlined.History,
                modifier = Modifier.weight(1f)
            )
        }

        SectionCard(
            title = "Próximas atenciones"
        ) {
            if (upcomingAppointments.isEmpty()) {
                Text(
                    text = "Este paciente no tiene citas activas.",
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                upcomingAppointments.forEachIndexed {
                        index, appointment ->

                    PatientAppointmentRow(
                        appointment = appointment
                    )

                    if (index != upcomingAppointments.lastIndex) {
                        HorizontalDivider(
                            color = colors.outlineVariant
                        )
                    }
                }
            }
        }

        SectionCard(
            title = "Historial de citas"
        ) {
            if (appointmentHistory.isEmpty()) {
                Text(
                    text = "No se encontraron atenciones anteriores.",
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                appointmentHistory.forEachIndexed {
                        index, appointment ->

                    PatientAppointmentRow(
                        appointment = appointment
                    )

                    if (index != appointmentHistory.lastIndex) {
                        HorizontalDivider(
                            color = colors.outlineVariant
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onNewAppointment,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.CalendarMonth,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Agendar nueva cita",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Tarjeta pequeña de estadísticas.
 */
@Composable
private fun PatientStatCard(
    count: Int,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(23.dp)
            )

            Text(
                text = count.toString(),
                style = MaterialTheme
                    .typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onPrimaryContainer
            )
        }
    }
}

/**
 * Vista compacta de cada cita.
 */
@Composable
private fun PatientAppointmentRow(
    appointment: Appointment
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement =
            Arrangement.spacedBy(9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.primary
            )

            Text(
                text = formatPatientDetailDate(
                    appointment.appointmentDate
                ),
                style = MaterialTheme
                    .typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant
            )

            Text(
                text = appointment.timeRange,
                style = MaterialTheme
                    .typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.LocalHospital,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant
            )

            Text(
                text = listOf(
                    appointment.doctorFullName,
                    appointment.specialityName
                ).filter {
                    it.isNotBlank()
                }.joinToString(" · "),
                style = MaterialTheme
                    .typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        AppointmentStatusBadge(
            appointment.status
        )
    }
}

/**
 * Acciones administrativas.
 */
@Composable
private fun PatientActionsSection(
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Acciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = onEditClick,
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Editar paciente")
        }

        OutlinedButton(
            onClick = onDeleteClick,
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme
                    .colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = if (isDeleting) {
                    "Eliminando..."
                } else {
                    "Eliminar paciente"
                }
            )
        }
    }
}

/**
 * Contenedor reutilizable para secciones.
 */
@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme
                    .typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            HorizontalDivider(
                color = colors.outlineVariant
            )

            content()
        }
    }
}

/**
 * Fila genérica de información del paciente.
 */
@Composable
private fun PatientInformationRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = colors.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = value.ifBlank {
                    "No especificado"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Iniciales del paciente.
 */
private fun patientInitials(
    fullName: String
): String {
    val words = fullName
        .trim()
        .split(Regex("\\s+"))
        .filter {
            it.isNotBlank()
        }

    return when {
        words.isEmpty() -> "P"

        words.size == 1 ->
            words.first().take(2).uppercase()

        else -> buildString {
            append(
                words.first().first().uppercaseChar()
            )

            append(
                words.last().first().uppercaseChar()
            )
        }
    }
}

/**
 * Fecha ISO a presentación en español.
 */
private fun formatPatientDetailDate(
    value: String
): String {
    val parts = value.split("-")

    if (parts.size != 3) {
        return value
    }

    val day = parts[2].toIntOrNull()
        ?: return value

    val month = parts[1].toIntOrNull()
        ?: return value

    val year = parts[0]

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

    val monthName = months.getOrNull(
        month - 1
    ) ?: return value

    return "$day de $monthName de $year"
}
