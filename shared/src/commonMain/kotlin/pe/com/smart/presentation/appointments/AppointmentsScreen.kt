
package pe.com.smart.presentation.appointments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.presentation.components.form.AppSearchField
import pe.com.smart.presentation.appointments.components.AppointmentActionsMenu
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.domain.model.Patient
import pe.com.smart.presentation.appointments.components.AppointmentStatusBadge
import pe.com.smart.presentation.appointments.components.appointmentStatusLabel
import pe.com.smart.presentation.appointments.components.appointmentStatusVisual
import pe.com.smart.presentation.schedules.scheduleFilterChipColors

/**
 * Pantalla principal de citas médicas.
 *
 * Los filtros se administran desde
 * AppointmentsViewModel y AppointmentsUiState.
 *
 * Conserva los parámetros utilizados
 * por AppNavigation.
 */
@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    initialCreate: Boolean = false,
    initialPatientId: Int? = null,
    initialDate: String? = null,
    onInitialCreateDismiss: () -> Unit = {},
    viewModel: AppointmentsViewModel = koinViewModel()
) {
    val state by viewModel.state
        .collectAsStateWithLifecycle()

    val claims = AccessTokenClaimsDecoder.decode(
        TokenStorage.getToken().orEmpty()
    )

    val isAdministrator =
        claims?.hasRole("ADMIN") == true

    val isDoctor =
        !isAdministrator &&
                claims?.hasRole("DOCTOR") == true

    val isPatient =
        !isAdministrator &&
                !isDoctor &&
                claims?.hasRole("PATIENT") == true

    val canCreate = isAdministrator || isPatient

    // Estados transitorios de navegación y diálogos.
    var editor by remember {
        mutableStateOf<Appointment?>(null)
    }

    var showEditor by remember {
        mutableStateOf(false)
    }

    var cancelling by remember {
        mutableStateOf<Appointment?>(null)
    }

    var confirming by remember {
        mutableStateOf<Appointment?>(null)
    }

    var completing by remember {
        mutableStateOf<Appointment?>(null)
    }

    var detail by remember {
        mutableStateOf<Appointment?>(null)
    }

    // Cargar información.
    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    // Apertura desde navegación.
    LaunchedEffect(initialCreate, canCreate) {
        if (initialCreate && canCreate) {
            editor = null
            showEditor = true
        }
    }

    val filtered = state.filteredAppointments

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading &&
                    state.appointments.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            state.error != null &&
                    state.appointments.isEmpty() -> {
                AppError(
                    message = state.error
                        ?: "No se pudieron cargar las citas.",
                    onRetry = {
                        viewModel.load()
                    }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 20.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        AppointmentHeader(
                            state = state,
                            isDoctor = isDoctor,
                            isPatient = isPatient,
                            canCreate = canCreate,
                            onQuery =
                                viewModel::onSearchChange,
                            onStatus =
                                viewModel::onStatusSelected,
                            onScope =
                                viewModel::onScopeSelected,
                            onClear =
                                viewModel::clearFilters,
                            onNew = {
                                editor = null
                                showEditor = true
                            }
                        )
                    }

                    if (state.isLoading) {
                        item {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )

                                Text(
                                    text = "Actualizando citas...",
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )
                            }
                        }
                    }

                    if (!state.error.isNullOrBlank()) {
                        item {
                            Surface(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(12.dp),
                                color = MaterialTheme
                                    .colorScheme.errorContainer
                            ) {
                                Text(
                                    text = state.error.orEmpty(),
                                    modifier =
                                        Modifier.padding(14.dp),
                                    color = MaterialTheme
                                        .colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            AppEmptyState(
                                title =
                                    "No se encontraron citas",
                                message =
                                    "No hay citas que coincidan con los filtros seleccionados."
                            )
                        }
                    } else {
                        filtered
                            .groupBy {
                                it.appointmentDate
                            }
                            .forEach { (date, appointments) ->

                                item(
                                    key = "date_$date"
                                ) {
                                    Text(
                                        text = friendlyDate(date),
                                        style = MaterialTheme
                                            .typography.titleSmall,
                                        fontWeight =
                                            FontWeight.Bold,
                                        color = MaterialTheme
                                            .colorScheme.onSurfaceVariant
                                    )
                                }

                                items(
                                    items = appointments,
                                    key = { it.id }
                                ) { appointment ->

                                    AppointmentCard(
                                        item = appointment,
                                        isPatient = isPatient,
                                        isDoctor = isDoctor,
                                        isAdministrator =
                                            isAdministrator,
                                        onOpen = {
                                            detail = appointment
                                        },
                                        onEdit = {
                                            editor = appointment
                                            showEditor = true
                                        },
                                        onConfirm = {
                                            confirming = appointment
                                        },
                                        onComplete = {
                                            completing = appointment
                                        },
                                        onCancel = {
                                            cancelling = appointment
                                        }
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    // =====================================================
    // EDITOR DE CITAS
    // =====================================================

    if (showEditor && canCreate) {
        AppointmentEditor(
            existing = editor,
            doctors = state.doctors,
            patients = state.patients,
            schedules = state.schedules,
            appointments = state.appointments,
            saving = state.isSaving,
            initialPatientId = initialPatientId,
            initialDate = initialDate,
            isPatient = isPatient,
            onDoctorSelected =
                viewModel::loadSchedulesForDoctor,
            onDismiss = {
                if (!state.isSaving) {
                    showEditor = false

                    if (initialCreate) {
                        onInitialCreateDismiss()
                    }
                }
            },
            onSave = {
                    reason: String,
                    date: String,
                    start: String,
                    end: String,
                    patientId: Int,
                    doctorId: Int ->

                viewModel.save(
                    existing = editor,
                    reason = reason,
                    date = date,
                    start = start,
                    end = end,
                    patientId = patientId,
                    doctorId = doctorId,
                    done = {
                        showEditor = false

                        if (initialCreate) {
                            onInitialCreateDismiss()
                        }
                    }
                )
            }
        )
    }

    // =====================================================
    // CANCELAR CITA
    // =====================================================

    AppConfirmDialog(
        visible = cancelling != null,
        title = "Cancelar cita",
        message = "¿Deseas cancelar la cita de ${
            cancelling?.patientFullName.orEmpty()
        }?",
        confirmText = "Cancelar cita",
        cancelText = "Volver",
        destructive = true,
        isLoading = state.isSaving,
        onConfirm = {
            if (!state.isSaving) {
                cancelling?.let { appointment ->
                    viewModel.changeStatus(
                        appointment,
                        "cancel"
                    )
                }
                cancelling = null
            }
        },
        onDismiss = {
            if (!state.isSaving) {
                cancelling = null
            }
        }
    )

    // =====================================================
    // CONFIRMAR CITA
    // =====================================================

    AppConfirmDialog(
        visible = confirming != null,
        title = "Confirmar cita",
        message = "¿Deseas confirmar esta cita médica?",
        confirmText = "Confirmar",
        isLoading = state.isSaving,
        onConfirm = {
            if (!state.isSaving && isAdministrator) {
                confirming?.let { appointment ->
                    viewModel.changeStatus(
                        appointment,
                        "confirm"
                    )
                }
                confirming = null
            }
        },
        onDismiss = {
            if (!state.isSaving) {
                confirming = null
            }
        }
    )

    // =====================================================
    // COMPLETAR CITA
    // =====================================================

    AppConfirmDialog(
        visible = completing != null,
        title = "Completar atención",
        message = "¿Deseas marcar esta atención como completada?",
        confirmText = "Completar",
        isLoading = state.isSaving,
        onConfirm = {
            if (
                !state.isSaving &&
                (isAdministrator || isDoctor)
            ) {
                completing?.let { appointment ->
                    viewModel.changeStatus(
                        appointment,
                        "complete"
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

    // =====================================================
    // DETALLE DE CITA
    // =====================================================

    detail?.let { appointment ->
        AppointmentDetailSheet(
            appointment = appointment,
            isPatient = isPatient,
            isDoctor = isDoctor,
            isAdministrator = isAdministrator,
            onDismiss = {
                detail = null
            },
            onEdit = {
                detail = null
                editor = appointment
                showEditor = true
            },
            onConfirm = {
                detail = null
                confirming = appointment
            },
            onComplete = {
                detail = null
                completing = appointment
            },
            onCancel = {
                detail = null
                cancelling = appointment
            }
        )
    }
}

// =========================================================
// FILTROS
// =========================================================

@Composable
private fun AppointmentHeader(
    state: AppointmentsUiState,
    isDoctor: Boolean,
    isPatient: Boolean,
    canCreate: Boolean,
    onQuery: (String) -> Unit,
    onStatus: (String?) -> Unit,
    onScope: (AppointmentListScope) -> Unit,
    onClear: () -> Unit,
    onNew: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = when {
                isDoctor -> "Mi agenda"
                isPatient -> "Mis citas"
                else -> "Gestión de citas"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = when {
                isDoctor ->
                    "Consulta las atenciones que tienes asignadas."

                isPatient ->
                    "Consulta y administra tus reservas médicas."

                else ->
                    "Gestiona las citas y realiza el seguimiento de las atenciones."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )

        if (canCreate) {
            Button(
                onClick = onNew,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null
                )

                Spacer(Modifier.size(8.dp))

                Text("Nueva cita")
            }
        }

        // La consulta actual del médico recibe
        // solamente citas confirmadas.
        if (!isDoctor) {
            Text(
                text = "Tipo de citas",
                style = MaterialTheme
                    .typography.labelLarge
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AppointmentListScope.ACTIVE to "Activas",
                    AppointmentListScope.HISTORY to "Historial",
                    AppointmentListScope.ALL to "Todas"
                ).forEach { (scope, label) ->
                    FilterChip(
                        selected =
                            state.selectedScope == scope,
                        onClick = {
                            onScope(scope)
                        },
                        label = {
                            Text(label)
                        },
                        colors =
                            scheduleFilterChipColors()
                    )
                }
            }

            // Los estados se adaptan al grupo.
            Text(
                text = "Estado",
                style = MaterialTheme
                    .typography.labelLarge
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected =
                        state.selectedStatus == null,
                    onClick = {
                        onStatus(null)
                    },
                    label = {
                        Text(
                            "Todos (${state.scopedAppointments.size})"
                        )
                    },
                    colors = scheduleFilterChipColors()
                )

                state.availableStatuses.forEach { status ->
                    val count = state.scopedAppointments
                        .count {
                            it.status == status
                        }

                    val statusVisual = appointmentStatusVisual(status)

                    FilterChip(
                        selected =
                            state.selectedStatus == status,
                        onClick = {
                            onStatus(status)
                        },
                        label = {
                            Text(
                                "${appointmentStatusLabel(status)} ($count)"
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor =
                                statusVisual.container,
                            selectedLabelColor =
                                statusVisual.content,
                            selectedLeadingIconColor =
                                statusVisual.content
                        )
                    )
                }
            }
        } else {
            Text(
                text = "Agenda de citas confirmadas",
                style = MaterialTheme
                    .typography.labelLarge,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )
        }

        AppSearchField(
            query = state.searchQuery,
            onQueryChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Paciente, médico o fecha",
            contentDescription = "Buscar cita"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.filteredAppointments.size} citas",
                style = MaterialTheme
                    .typography.labelMedium,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )

            if (
                state.selectedScope !=
                AppointmentListScope.ACTIVE ||
                state.selectedStatus != null ||
                state.searchQuery.isNotBlank()
            ) {
                OutlinedButton(
                    onClick = onClear
                ) {
                    Text("Limpiar filtros")
                }
            }
        }
    }
}

// =========================================================
// TARJETA DE CITA
// =========================================================

@Composable
private fun AppointmentCard(
    item: Appointment,
    isPatient: Boolean,
    isDoctor: Boolean,
    isAdministrator: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val canEdit = isAdministrator &&
            item.status == "SCHEDULED"

    val canConfirm = isAdministrator &&
            item.status == "SCHEDULED"

    val canComplete =
        (isAdministrator || isDoctor) &&
                item.status == "CONFIRMED"

    val canCancel =
        (isAdministrator || isPatient) &&
                item.status in setOf(
            "SCHEDULED",
            "CONFIRMED"
        )

    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.timeRange,
                        style = MaterialTheme
                            .typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    Text(
                        text = if (isPatient) {
                            item.doctorFullName
                        } else {
                            item.patientFullName
                        },
                        style = MaterialTheme
                            .typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (isPatient) {
                            item.specialityName
                        } else {
                            "${item.doctorFullName} · ${item.specialityName}"
                        },
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                if (
                    canEdit ||
                    canConfirm ||
                    canComplete ||
                    canCancel
                ) {
                    AppointmentActionsMenu(
                        appointmentId = item.id,
                        canEdit = canEdit,
                        canConfirm = canConfirm,
                        canComplete = canComplete,
                        canCancel = canCancel,
                        onEdit = onEdit,
                        onConfirm = onConfirm,
                        onComplete = onComplete,
                        onCancel = onCancel
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            if (item.reason.isNotBlank()) {
                Text(
                    text = item.reason,
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AppointmentStatusBadge(
                status = item.status
            )

        }
    }
}

// =========================================================
// DETALLE DE CITA
// =========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentDetailSheet(
    appointment: Appointment,
    isPatient: Boolean,
    isDoctor: Boolean,
    isAdministrator: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Detalle de la cita",
                style = MaterialTheme
                    .typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${friendlyDate(appointment.appointmentDate)} · ${appointment.timeRange}",
                style = MaterialTheme
                    .typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            AppointmentStatusBadge(
                status = appointment.status
            )

            AppointmentInfoRow(
                label = "Paciente",
                value = appointment.patientFullName
            )

            AppointmentInfoRow(
                label = "Médico",
                value = appointment.doctorFullName
            )

            AppointmentInfoRow(
                label = "Especialidad",
                value = appointment.specialityName
            )

            AppointmentInfoRow(
                label = "Motivo de consulta",
                value = appointment.reason
            )

            if (
                isPatient &&
                appointment.status in setOf(
                    "SCHEDULED",
                    "CONFIRMED"
                )
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar cita")
                }
            }

            if (
                isDoctor &&
                appointment.status == "CONFIRMED"
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Completar atención")
                }
            }

            if (isAdministrator) {
                when (appointment.status) {
                    "SCHEDULED" -> {
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar cita")
                        }

                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reprogramar")
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar cita")
                        }
                    }

                    "CONFIRMED" -> {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Completar atención")
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar cita")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar")
            }
        }
    }
}

@Composable
private fun AppointmentInfoRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme
                .typography.labelLarge,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )

        Text(
            text = value.ifBlank {
                "No especificado"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// =========================================================
// EDITOR DE CITAS
// =========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentEditor(
    existing: Appointment?,
    doctors: List<Doctor>,
    patients: List<Patient>,
    schedules: List<MedicalSchedule>,
    appointments: List<Appointment>,
    saving: Boolean,
    initialPatientId: Int?,
    initialDate: String?,
    isPatient: Boolean,
    onDoctorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        String,
        String,
        Int,
        Int
    ) -> Unit
) {
    val totalSteps = if (isPatient) 3 else 4
    val firstStep = if (!isPatient && (initialPatientId != null || existing != null)) 2 else 1
    var step by remember(existing, isPatient, initialPatientId) {
        // Al abrir desde la ficha del paciente, Administración ya tiene el
        // primer paso resuelto y puede ir directamente a la disponibilidad.
        // En una reprogramación no se cambia el paciente asociado.
        mutableStateOf(firstStep)
    }

    var speciality by remember(existing) {
        mutableStateOf(
            existing?.specialityName ?: ""
        )
    }

    var specialityQuery by remember(existing) {
        mutableStateOf("")
    }

    var doctorId by remember(existing) {
        mutableStateOf(existing?.doctorId)
    }

    var patientId by remember(
        existing,
        initialPatientId,
        isPatient
    ) {
        mutableStateOf(
            existing?.patientId
                ?: initialPatientId
                ?: if (isPatient) 0 else null
        )
    }

    var date by remember(existing, initialDate) {
        mutableStateOf(
            existing?.appointmentDate
                ?: initialDate.orEmpty()
        )
    }

    var slot by remember(existing) {
        mutableStateOf(
            existing?.let {
                it.startTime.take(5) to
                        it.endTime.take(5)
            }
        )
    }

    var reason by remember(existing) {
        mutableStateOf(existing?.reason ?: "")
    }

    var patientQuery by remember(existing) {
        mutableStateOf("")
    }

    val patientFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var error by remember(existing) {
        mutableStateOf<String?>(null)
    }

    var showDiscardDialog by remember(existing) {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showDoctorPicker by remember {
        mutableStateOf(false)
    }

    val selectedDoctor = doctors.firstOrNull {
        it.id == doctorId
    }

    val selectedPatient = patients.firstOrNull {
        it.id == patientId
    }

    val availableDoctors = doctors.filter { doctor ->
        !doctor.deleted &&
                (speciality.isBlank() ||
                        doctor.specialityName == speciality)
    }

    val specialities = doctors
        .filter { !it.deleted }
        .map { it.specialityName }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val specialitySearch = specialityQuery.trim()
    val matchingSpecialities = if (specialitySearch.isNotBlank()) {
        specialities.filter { name ->
            name.contains(specialitySearch, ignoreCase = true)
        }
    } else {
        specialities
    }

    val patientSearch = patientQuery.trim()
    val matchingPatients = if (patientSearch.length >= 2) {
        patients.filter { patient ->
            listOf(patient.fullName, patient.dni, patient.phone)
                .any { value ->
                    value.contains(patientSearch, ignoreCase = true)
                }
        }
    } else {
        emptyList()
    }

    val day = dayCode(date)

    LaunchedEffect(
        step,
        selectedPatient?.id,
        existing,
        isPatient
    ) {
        if (
            !isPatient &&
                    existing == null &&
                    step == 1 &&
                    selectedPatient == null
        ) {
            patientFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val available = if (
        selectedDoctor != null &&
        day != null
    ) {
        appointmentSlots(
            schedules = schedules.filter {
                it.doctorId == selectedDoctor.id &&
                        it.dayOfWeek == day
            },
            occupied = appointments.filter {
                it.doctorId == selectedDoctor.id &&
                        it.appointmentDate == date &&
                        it.status in setOf(
                    "SCHEDULED",
                    "CONFIRMED"
                ) &&
                        it.id != existing?.id
            }
        )
    } else {
        emptyList()
    }

    val hasDraft = existing != null ||
            doctorId != null ||
            slot != null ||
            reason.isNotBlank() ||
            (!isPatient && patientId != null)

    val requestDismiss = {
        if (!saving) {
            if (hasDraft) {
                showDiscardDialog = true
            } else {
                onDismiss()
            }
        }
    }

    // Seleccionar el primer intervalo disponible
    // cuando se está registrando una cita nueva.
    LaunchedEffect(
        selectedDoctor?.id,
        date,
        available
    ) {
        if (existing == null) {
            if (slot !in available) {
                slot = available.firstOrNull()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            requestDismiss()
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (existing == null) {
                        "Nueva cita"
                    } else {
                        "Reprogramar cita"
                    },
                    style = MaterialTheme
                        .typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = requestDismiss,
                    enabled = !saving
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }

            Text(
                text = if (isPatient) {
                    "Reserva en pocos pasos y revisa los datos antes de enviar tu solicitud."
                } else {
                    "Primero identifica al paciente y luego reserva un horario disponible."
                },
                style = MaterialTheme
                    .typography.bodyMedium,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )

            BookingProgress(
                currentStep = step,
                totalSteps = totalSteps,
                isPatient = isPatient
            )

            // =============================================
            // 1. PACIENTE (ADMINISTRACIÓN)
            // =============================================

            if (!isPatient && existing == null && step == 1) {
                EditorSection(number = "1", title = "Selecciona al paciente") {
                    OutlinedTextField(
                        value = selectedPatient?.fullName ?: patientQuery,
                        onValueChange = { value ->
                            // Al editar una selección, esta se descarta de
                            // inmediato para que el nuevo texto sea una búsqueda.
                            if (selectedPatient != null) {
                                patientId = null
                            }
                            patientQuery = value
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(patientFocusRequester),
                        label = { Text("Buscar por nombre, DNI o teléfono") },
                        placeholder = { Text("Escribe al menos 2 caracteres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (selectedPatient != null || patientQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        patientId = null
                                        patientQuery = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Limpiar paciente"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (selectedPatient != null) {
                        Text(
                            "Paciente seleccionado · DNI ${selectedPatient.dni} · ${selectedPatient.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (patientSearch.length >= 2) {
                        when {
                            matchingPatients.isEmpty() -> Text(
                                "No encontramos pacientes con esos datos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            else -> {
                                Text(
                                    "Coincidencias (${matchingPatients.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                matchingPatients.take(5).forEach { patient ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                patientId = patient.id
                                                patientQuery = ""
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                patient.fullName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "DNI ${patient.dni} · ${patient.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (matchingPatients.size > 5) {
                                    Text(
                                        "Mostrando 5 de ${matchingPatients.size}. Sigue escribiendo para refinar la búsqueda.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "Escribe al menos 2 caracteres para buscar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =============================================
            // MÉDICO, FECHA Y HORARIO
            // =============================================

            val schedulingStep = if (isPatient) 1 else 2
            if (step == schedulingStep) {
                EditorSection(number = schedulingStep.toString(), title = "Disponibilidad") {
                    Text("Especialidad", style = MaterialTheme.typography.labelLarge)

                    OutlinedTextField(
                        value = speciality.ifBlank { specialityQuery },
                        onValueChange = { value ->
                            if (speciality.isNotBlank()) {
                                speciality = ""
                            }
                            specialityQuery = value
                            doctorId = null
                            date = ""
                            slot = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar especialidad") },
                        placeholder = { Text("Escribe al menos 2 caracteres") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (speciality.isNotBlank() || specialityQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        speciality = ""
                                        specialityQuery = ""
                                        doctorId = null
                                        date = ""
                                        slot = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Limpiar especialidad"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    when {
                        speciality.isNotBlank() -> Text(
                            "Especialidad seleccionada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        matchingSpecialities.isEmpty() -> Text(
                            "No encontramos especialidades con esos datos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        else -> {
                            Text(
                                if (specialitySearch.isBlank()) {
                                    "Especialidades disponibles (${matchingSpecialities.size})"
                                } else {
                                    "Coincidencias (${matchingSpecialities.size})"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            matchingSpecialities.take(5).forEach { name ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            speciality = name
                                            specialityQuery = ""
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        name,
                                        modifier = Modifier.padding(14.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (matchingSpecialities.size > 5) {
                                Text(
                                    "Mostrando 5 de ${matchingSpecialities.size}. Escribe para filtrar la lista.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showDoctorPicker = true },
                        enabled = availableDoctors.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                        Text(
                            text = selectedDoctor?.fullName
                                ?: "Buscar y seleccionar médico",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (selectedDoctor != null) {
                        Text(
                            text = "El calendario mostrará únicamente días con turnos disponibles para ${selectedDoctor.fullName}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        enabled = selectedDoctor != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                        Text(
                            if (date.isBlank()) {
                                if (selectedDoctor == null) {
                                    " Primero selecciona un médico"
                                } else {
                                    " Seleccionar fecha disponible"
                                }
                            } else {
                                " ${friendlyDate(date)}"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    when {
                        selectedDoctor == null -> Text(
                            "Busca y selecciona un médico para consultar su disponibilidad.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        day == null -> Text(
                            "Selecciona una fecha disponible en el calendario.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        available.isEmpty() -> Text(
                            "No hay horarios disponibles para esta fecha. Elige otro día resaltado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> available.chunked(2).forEach { slotRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                slotRow.forEach { value ->
                                    FilterChip(
                                        selected = slot == value,
                                        onClick = { slot = value },
                                        label = { Text("${value.first}–${value.second}") },
                                        modifier = Modifier.weight(1f),
                                        colors = scheduleFilterChipColors()
                                    )
                                }
                                if (slotRow.size == 1) Box(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // =============================================
            // MOTIVO
            // =============================================

            val reasonStep = if (isPatient) 2 else 3
            if (step == reasonStep) {
                EditorSection(number = reasonStep.toString(), title = "Motivo de la consulta") {
                    Text(
                        "Describe brevemente el motivo. No incluyas diagnósticos ni datos clínicos sensibles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { value ->
                        if (value.length <= 500) {
                            reason = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Motivo de la cita")
                    },
                    supportingText = {
                        Text("${reason.length}/500")
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                }
            }

            if (step == totalSteps) {
                EditorSection(number = totalSteps.toString(), title = "Revisa la cita") {
                    if (!isPatient) {
                        BookingSummaryRow("Paciente", selectedPatient?.fullName ?: "No seleccionado")
                    }
                    BookingSummaryRow("Especialidad", speciality.ifBlank { selectedDoctor?.specialityName.orEmpty() })
                    BookingSummaryRow("Médico", selectedDoctor?.fullName.orEmpty())
                    BookingSummaryRow("Fecha", date.takeIf { it.isNotBlank() }?.let(::friendlyDate).orEmpty())
                    BookingSummaryRow("Horario", slot?.let { "${it.first}–${it.second}" }.orEmpty())
                    BookingSummaryRow("Motivo", reason.trim())
                    Text(
                        if (isPatient) "Tu solicitud quedará programada para validación." else "La cita quedará programada y podrás confirmarla después.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // =============================================
            // VALIDACIÓN
            // =============================================

            error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme
                        .typography.bodySmall
                )
            }

            val stepError = when {
                !isPatient && step == 1 && patientId == null -> "Selecciona un paciente."
                step == schedulingStep && selectedDoctor == null -> "Selecciona un médico."
                step == schedulingStep && day == null -> "Selecciona una fecha válida."
                step == schedulingStep && slot == null -> "Selecciona un horario."
                step == schedulingStep && existing == null && slot !in available -> "Selecciona un horario disponible."
                step == reasonStep && reason.isBlank() -> "Ingresa el motivo de la consulta."
                else -> null
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > firstStep) {
                    OutlinedButton(
                        onClick = { step-- },
                        enabled = !saving,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Volver")
                    }
                }

                Button(
                onClick = {
                    error = stepError
                    if (stepError == null && step < totalSteps) {
                        step++
                    } else if (stepError == null) {
                        val selectedSlot =
                            slot ?: return@Button

                        val selectedDoctorId =
                            doctorId ?: return@Button

                        onSave(
                            reason.trim(),
                            date,
                            selectedSlot.first,
                            selectedSlot.second,
                            patientId ?: 0,
                            selectedDoctorId
                        )
                    }
                },
                enabled = !saving,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme
                            .colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (step < totalSteps) {
                            "Continuar"
                        } else if (existing == null) {
                            if (isPatient) "Enviar solicitud" else "Programar cita"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }
            }

            Spacer(Modifier.size(8.dp))
        }
    }

    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "¿Descartar cita?",
        message = "Perderás la información ingresada. ¿Deseas salir sin guardar?",
        confirmText = "Descartar",
        cancelText = "Seguir editando",
        loadingText = "Saliendo...",
        icon = Icons.Outlined.WarningAmber,
        destructive = true,
        isLoading = false,
        onConfirm = {
            showDiscardDialog = false
            onDismiss()
        },
        onDismiss = {
            showDiscardDialog = false
        }
    )

    // =====================================================
    // SELECCIÓN DE FECHA
    // =====================================================

    if (showDoctorPicker) {
        DoctorSearchPicker(
            doctors = availableDoctors,
            selectedDoctorId = doctorId,
            onSelected = { selectedId ->
                val doctor = doctors.firstOrNull {
                    it.id == selectedId
                }
                doctorId = selectedId
                speciality = doctor?.specialityName.orEmpty()
                specialityQuery = ""
                date = ""
                slot = null
                showDoctorPicker = false
                onDoctorSelected(selectedId)
            },
            onDismiss = { showDoctorPicker = false }
        )
    }

    if (showDatePicker && selectedDoctor != null) {
        val selectedDoctorId = selectedDoctor.id
        val selectableDates = remember(
            selectedDoctorId,
            schedules,
            appointments,
            existing?.id
        ) {
            object : SelectableDates {
                override fun isSelectableDate(
                    utcTimeMillis: Long
                ): Boolean {
                    val candidate = epochDateToIso(utcTimeMillis)

                    return candidate >= bookingTodayIso() &&
                            hasAvailableSlotsOnDate(
                                date = candidate,
                                doctorId = selectedDoctorId,
                                schedules = schedules,
                                appointments = appointments,
                                excludedAppointmentId = existing?.id
                            )
                }
            }
        }
        val pickerState = rememberDatePickerState(
            selectableDates = selectableDates
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        pickerState.selectedDateMillis
                            ?.let { millis ->
                                date = epochDateToIso(
                                    millis
                                )
                                slot = null
                            }

                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        "Fechas disponibles",
                        modifier = Modifier.padding(
                            start = 24.dp,
                            top = 16.dp
                        )
                    )
                },
                headline = {
                    Text(
                        "Selecciona un día con turnos",
                        modifier = Modifier.padding(
                            start = 24.dp,
                            bottom = 12.dp
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun BookingProgress(
    currentStep: Int,
    totalSteps: Int,
    isPatient: Boolean
) {
    val labels = if (isPatient) {
        listOf("Horario", "Motivo", "Revisar")
    } else {
        listOf("Paciente", "Horario", "Motivo", "Revisar")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index + 1 == currentStep
            val complete = index + 1 < currentStep
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = when {
                    selected -> MaterialTheme.colorScheme.primaryContainer
                    complete -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = "${index + 1}. $label",
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BookingSummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value.ifBlank { "No seleccionado" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// =========================================================
// SECCIÓN DEL FORMULARIO
// =========================================================

@Composable
private fun EditorSection(
    number: String,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme
                .colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme
                        .colorScheme.primaryContainer
                ) {
                    Text(
                        text = number,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        color = MaterialTheme
                            .colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = title,
                    modifier =
                        Modifier.padding(start = 10.dp),
                    style = MaterialTheme
                        .typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            content()
        }
    }
}

// =========================================================
// BUSCADOR DE MÉDICOS
// =========================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorSearchPicker(
    doctors: List<Doctor>,
    selectedDoctorId: Int?,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val normalizedQuery = query.trim()
    val matches = doctors.filter { doctor ->
        normalizedQuery.isBlank() ||
                listOf(
                    doctor.fullName,
                    doctor.specialityName,
                    doctor.email
                ).any { value ->
                    value.contains(normalizedQuery, ignoreCase = true)
                }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Seleccionar médico",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Busca por nombre, especialidad o correo y toca un resultado para seleccionarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                label = { Text("Buscar médico") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(14.dp)
            )

            if (matches.isEmpty()) {
                AppEmptyState(
                    title = "No hay resultados",
                    message = "Prueba con otro nombre o especialidad."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches, key = { it.id }) { doctor ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(doctor.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (doctor.id == selectedDoctorId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    doctor.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    doctor.specialityName.ifBlank { "Sin especialidad registrada" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// HORARIOS DISPONIBLES
// =========================================================

/**
 * Calcula intervalos a partir de los horarios
 * configurados para un médico.
 *
 * Descarta intervalos ocupados por citas
 * SCHEDULED o CONFIRMED.
 *
 * El backend debe validar nuevamente
 * la disponibilidad antes de guardar.
 */
private fun appointmentSlots(
    schedules: List<MedicalSchedule>,
    occupied: List<Appointment>
): List<Pair<String, String>> {
    return schedules
        .filter {
            it.appointmentDuration > 0
        }
        .sortedBy {
            it.startTime
        }
        .flatMap { schedule ->
            generateSequence(
                schedule.startDisplay
            ) { previous ->
                addMinutes(
                    previous,
                    schedule.appointmentDuration
                )
            }
                .takeWhile { start ->
                    addMinutes(
                        start,
                        schedule.appointmentDuration
                    ) <= schedule.endDisplay
                }
                .map { start ->
                    start to addMinutes(
                        start,
                        schedule.appointmentDuration
                    )
                }
                .filter { candidate ->
                    occupied.none { appointment ->
                        appointment.startTime.take(5) <
                                candidate.second &&
                                appointment.endTime.take(5) >
                                candidate.first
                    }
                }
                .toList()
        }
        .distinct()
        .sortedBy {
            it.first
        }
}

/**
 * Indica si un día puede reservarse para un médico: debe existir un
 * horario configurado y, tras descartar las citas activas, al menos un
 * intervalo libre. Se usa por el DatePicker para deshabilitar el resto.
 */
private fun hasAvailableSlotsOnDate(
    date: String,
    doctorId: Int,
    schedules: List<MedicalSchedule>,
    appointments: List<Appointment>,
    excludedAppointmentId: Int?
): Boolean {
    val day = dayCode(date) ?: return false

    val doctorSchedules = schedules.filter {
        it.doctorId == doctorId && it.dayOfWeek == day
    }

    if (doctorSchedules.isEmpty()) return false

    val occupied = appointments.filter {
        it.doctorId == doctorId &&
                it.appointmentDate == date &&
                it.status in setOf("SCHEDULED", "CONFIRMED") &&
                it.id != excludedAppointmentId
    }

    return appointmentSlots(
        schedules = doctorSchedules,
        occupied = occupied
    ).isNotEmpty()
}

private fun addMinutes(
    value: String,
    minutes: Int
): String {
    val total =
        value.take(2).toInt() * 60 +
                value.substring(3, 5).toInt() +
                minutes

    return twoDigits(total / 60) +
            ":" +
            twoDigits(total % 60)
}

private fun twoDigits(
    value: Int
): String {
    return value.toString().padStart(2, '0')
}

// =========================================================
// CÓDIGO DEL DÍA DE LA SEMANA
// =========================================================

/**
 * Convierte YYYY-MM-DD en:
 * MONDAY, TUESDAY, etc.
 */
private fun dayCode(
    date: String
): String? {
    val parts = date.split("-")

    val year = parts.getOrNull(0)
        ?.toIntOrNull() ?: return null

    val month = parts.getOrNull(1)
        ?.toIntOrNull() ?: return null

    val day = parts.getOrNull(2)
        ?.toIntOrNull() ?: return null

    if (
        date.length != 10 ||
        month !in 1..12 ||
        day !in 1..31
    ) {
        return null
    }

    val table = intArrayOf(
        0, 3, 2, 5, 0, 3,
        5, 1, 4, 6, 2, 4
    )

    val adjustedYear =
        if (month < 3) year - 1 else year

    val index = (
            adjustedYear +
                    adjustedYear / 4 -
                    adjustedYear / 100 +
                    adjustedYear / 400 +
                    table[month - 1] +
                    day
            ) % 7

    return listOf(
        "SUNDAY",
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY"
    )[index]
}

// =========================================================
// FORMATO AMIGABLE DE FECHAS
// =========================================================

/**
 * Ejemplo:
 * 2026-11-17 -> 17 de nov. de 2026
 */
private fun friendlyDate(
    value: String
): String {
    val parts = value.split("-")

    if (parts.size != 3) {
        return value
    }

    val month = listOf(
        "ene.",
        "feb.",
        "mar.",
        "abr.",
        "may.",
        "jun.",
        "jul.",
        "ago.",
        "sep.",
        "oct.",
        "nov.",
        "dic."
    ).getOrNull(
        parts[1].toIntOrNull()
            ?.minus(1) ?: -1
    ) ?: return value

    val day = parts[2].toIntOrNull()
        ?: return value

    return "$day de $month de ${parts[0]}"
}

// =========================================================
// CONVERSIÓN DE FECHA DEL DATEPICKER
// =========================================================

private fun bookingTodayIso(): String = Instant
    .fromEpochMilliseconds(
        kotlin.time.Clock.System.now()
            .toEpochMilliseconds()
    )
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
    .toString()

/**
 * Convierte la fecha UTC de Material3 DatePicker
 * al formato ISO YYYY-MM-DD.
 *
 * No depende de la zona horaria local.
 */
private fun epochDateToIso(
    millis: Long
): String {
    val epochDays = millis / 86_400_000L

    val z = epochDays + 719468L

    val era = if (z >= 0) {
        z / 146097L
    } else {
        (z - 146096L) / 146097L
    }

    val doe = z - era * 146097L

    val yoe = (
            doe -
                    doe / 1460L +
                    doe / 36524L -
                    doe / 146096L
            ) / 365L

    var year = yoe + era * 400L

    val doy = doe - (
            365L * yoe +
                    yoe / 4L -
                    yoe / 100L
            )

    val monthPart = (5L * doy + 2L) / 153L

    val day =
        doy -
                (153L * monthPart + 2L) / 5L +
                1L

    val month =
        monthPart +
                if (monthPart < 10L) 3L else -9L

    if (month <= 2L) {
        year++
    }

    return "$year-" +
            "${twoDigits(month.toInt())}-" +
            twoDigits(day.toInt())
}
