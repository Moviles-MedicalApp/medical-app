package pe.com.smart.presentation.patients.detail

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
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.domain.model.Patient
import pe.com.smart.domain.model.Appointment

@Composable
fun PatientDetailScreen(
    patientId: Int,
    refreshKey: Int = 0,
    onEditClick: (Int) -> Unit,
    onNewAppointmentClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel: PatientDetailViewModel =
        viewModel(
            key = "patient_detail_$patientId"
        ) {
            PatientDetailViewModel(
                patientId = patientId
            )
        }

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by
    remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        patientId,
        refreshKey
    ) {
        viewModel.loadPatient()
    }

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        when {

            /*
             * =================================================
             * LOADING
             * =================================================
             */
            uiState.isLoading &&
                    uiState.patient == null -> {

                AppLoading(
                    message = "Cargando paciente..."
                )
            }

            /*
             * =================================================
             * ERROR
             * =================================================
             */
            uiState.error != null &&
                    uiState.patient == null -> {

                AppError(
                    message =
                        uiState.error
                            ?: "No se pudo cargar el paciente.",

                    onRetry =
                        viewModel::loadPatient
                )
            }

            /*
             * =================================================
             * CONTENIDO
             * =================================================
             */
            uiState.patient != null -> {

                PatientDetailContent(
                    patient = uiState.patient!!,
                    upcomingAppointments = uiState.upcomingAppointments,
                    appointmentHistory = uiState.appointmentHistory,
                    isDeleting = uiState.isDeleting,
                    error = uiState.error,
                    onEditClick = {
                        onEditClick(
                            uiState.patient!!.id
                        )
                    },
                    onNewAppointmentClick = { onNewAppointmentClick(uiState.patient!!.id) },
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    /*
     * =====================================================
     * CONFIRMAR ELIMINACIÓN
     * =====================================================
     */
    AppConfirmDialog(
        visible =
            showDeleteDialog,

        title =
            "Eliminar paciente",

        message =
            "¿Estás seguro de que deseas eliminar este paciente? Esta acción no se puede deshacer.",

        confirmText =
            "Eliminar",

        cancelText =
            "Cancelar",

        loadingText =
            "Eliminando...",

        icon =
            Icons.Outlined.Delete,

        destructive =
            true,

        isLoading =
            uiState.isDeleting,

        onConfirm = {

            viewModel.deletePatient(
                onSuccess = {

                    showDeleteDialog =
                        false

                    onDeleted()
                }
            )
        },

        onDismiss = {

            showDeleteDialog =
                false
        }
    )
}

@Composable
private fun PatientAppointmentsSummary(
    upcoming: List<Appointment>,
    history: List<Appointment>,
    onNewAppointment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Citas del paciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${upcoming.size} activas · ${history.size} en historial",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            upcoming.firstOrNull()?.let { appointment ->
                Text("Próxima: ${appointment.appointmentDate} · ${appointment.timeRange}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text("${appointment.doctorFullName} · ${appointment.specialityName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } ?: Text("No tiene citas activas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (history.isNotEmpty()) {
                Text("Últimas atenciones", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                history.take(3).forEach { appointment ->
                    Text(
                        "${appointment.appointmentDate} · ${appointment.timeRange} · ${appointment.specialityName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(onClick = onNewAppointment, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agendar cita")
            }
        }
    }
}


/*
 * =====================================================
 * CONTENIDO DEL DETALLE
 * =====================================================
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
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                20.dp
            )
    ) {

        /*
         * =================================================
         * TARJETA PRINCIPAL
         * =================================================
         */
        Card(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    22.dp
                ),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                ),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation =
                        3.dp
                )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            20.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        18.dp
                    )
            ) {

                /*
                 * =========================================
                 * CABECERA
                 * =========================================
                 */
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(
                                58.dp
                            ),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme
                                .primaryContainer
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.Badge,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        28.dp
                                    ),

                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                16.dp
                            )
                    )

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                4.dp
                            )
                    ) {

                        Text(
                            text =
                                patient.fullName,

                            style =
                                MaterialTheme.typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurface
                        )

                        Text(
                            text =
                                "DNI: ${patient.dni}",

                            style =
                                MaterialTheme.typography
                                    .bodyMedium,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

                /*
                 * =========================================
                 * CONTACTO
                 * =========================================
                 */

                PatientDetailRow(
                    icon =
                        Icons.Outlined.Email,

                    label =
                        "Correo electrónico",

                    value =
                        patient.email
                )

                PatientDetailRow(
                    icon =
                        Icons.Outlined.Phone,

                    label =
                        "Teléfono",

                    value =
                        patient.phone
                )
            }
        }

        /*
         * =================================================
         * ERROR DE RECARGA
         * =================================================
         */
        if (!error.isNullOrBlank()) {

            Text(
                text =
                    error,

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.error
            )
        }

        PatientAppointmentsSummary(upcomingAppointments, appointmentHistory, onNewAppointmentClick)

        /* SECCIÓN DE ACCIONES */
        Text(
            text =
                "Acciones",

            style =
                MaterialTheme.typography.titleMedium,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme.colorScheme.onBackground
        )

        /* EDITAR */
        Button(
            onClick =
                onEditClick,

            enabled =
                !isDeleting,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                MaterialTheme.shapes.extraLarge
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Edit,

                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    "Editar paciente",

                fontWeight =
                    FontWeight.SemiBold
            )
        }

        /*
         * ELIMINAR
         */
        OutlinedButton(
            onClick =
                onDeleteClick,

            enabled =
                !isDeleting,

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                MaterialTheme.shapes.extraLarge,

            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor =
                        MaterialTheme.colorScheme.error
                )
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Delete,

                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    if (isDeleting) {
                        "Eliminando..."
                    } else {
                        "Eliminar paciente"
                    },

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}


/*
 * =====================================================
 * FILA DE INFORMACIÓN
 * =====================================================
 */

@Composable
private fun PatientDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(
            modifier =
                Modifier.size(
                    42.dp
                ),

            shape =
                RoundedCornerShape(
                    12.dp
                ),

            color =
                MaterialTheme.colorScheme
                    .surfaceVariant
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
                        Modifier.size(
                            22.dp
                        ),

                    tint =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(
                    14.dp
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(
                text =
                    label,

                style =
                    MaterialTheme.typography.labelMedium,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text =
                    value,

                style =
                    MaterialTheme.typography.bodyLarge,

                color =
                    MaterialTheme.colorScheme
                        .onSurface
            )
        }
    }
}
