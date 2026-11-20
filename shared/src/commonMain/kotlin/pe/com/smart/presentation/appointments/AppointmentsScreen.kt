
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

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                        colors =
                            scheduleFilterChipColors()
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
    var speciality by remember(existing) {
        mutableStateOf(
            existing?.specialityName ?: ""
        )
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

    var error by remember(existing) {
        mutableStateOf<String?>(null)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val selectedDoctor = doctors.firstOrNull {
        it.id == doctorId
    }

    val day = dayCode(date)

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
            if (!saving) {
                onDismiss()
            }
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

            Text(
                text = if (isPatient) {
                    "Selecciona una especialidad, un médico y un horario."
                } else {
                    "Selecciona el médico, la fecha, el horario y el paciente."
                },
                style = MaterialTheme
                    .typography.bodyMedium,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )

            // =============================================
            // 1. DATOS DE LA ATENCIÓN
            // =============================================

            EditorSection(
                number = "1",
                title = "Datos de la atención"
            ) {
                Text(
                    text = "Especialidad",
                    style = MaterialTheme
                        .typography.labelLarge
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    doctors
                        .map {
                            it.specialityName
                        }
                        .distinct()
                        .filter {
                            it.isNotBlank()
                        }
                        .forEach { name ->
                            FilterChip(
                                selected =
                                    speciality == name,
                                onClick = {
                                    speciality = name
                                    doctorId = null
                                    slot = null
                                },
                                label = {
                                    Text(name)
                                },
                                colors =
                                    scheduleFilterChipColors()
                            )
                        }
                }

                Selector(
                    label = "Médico",
                    value = selectedDoctor?.fullName
                        ?: "Selecciona un médico",
                    options = doctors.filter {
                        speciality.isBlank() ||
                                it.specialityName == speciality
                    }.map {
                        it.id to it.fullName
                    },
                    onSelected = { selectedId ->
                        doctorId = selectedId
                        slot = null

                        onDoctorSelected(selectedId)
                    }
                )

                OutlinedButton(
                    onClick = {
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.CalendarMonth,
                        contentDescription = null
                    )

                    Text(
                        text = if (date.isBlank()) {
                            " Seleccionar fecha"
                        } else {
                            " ${friendlyDate(date)}"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // =============================================
            // 2. DISPONIBILIDAD
            // =============================================

            EditorSection(
                number = "2",
                title = "Horario disponible"
            ) {
                when {
                    selectedDoctor == null ||
                            day == null -> {
                        Text(
                            text = "Selecciona un médico y una fecha válida.",
                            color = MaterialTheme
                                .colorScheme.onSurfaceVariant
                        )
                    }

                    available.isEmpty() -> {
                        Text(
                            text = "No hay horarios disponibles para esta fecha.",
                            color = MaterialTheme
                                .colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        available
                            .chunked(2)
                            .forEach { slotRow ->
                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    slotRow.forEach { value ->
                                        FilterChip(
                                            selected =
                                                slot == value,
                                            onClick = {
                                                slot = value
                                            },
                                            label = {
                                                Text(
                                                    "${value.first}–${value.second}"
                                                )
                                            },
                                            modifier =
                                                Modifier.weight(1f),
                                            colors =
                                                scheduleFilterChipColors()
                                        )
                                    }

                                    if (slotRow.size == 1) {
                                        Box(
                                            modifier =
                                                Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                    }
                }

                slot?.let { selected ->
                    Text(
                        text = "Seleccionado: ${selected.first}–${selected.second}",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = MaterialTheme
                            .colorScheme.primary
                    )
                }
            }

            // =============================================
            // 3. PACIENTE Y MOTIVO
            // =============================================

            EditorSection(
                number = "3",
                title = if (isPatient) {
                    "Motivo de la consulta"
                } else {
                    "Paciente y motivo"
                }
            ) {
                if (!isPatient) {
                    Selector(
                        label = "Paciente",
                        value = patients.firstOrNull {
                            it.id == patientId
                        }?.fullName
                            ?: "Selecciona un paciente",
                        options = patients.map {
                            it.id to
                                    "${it.fullName} · ${it.dni}"
                        },
                        onSelected = { selectedId ->
                            patientId = selectedId
                        }
                    )
                }

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

            Button(
                onClick = {
                    error = when {
                        selectedDoctor == null ->
                            "Selecciona un médico."

                        !isPatient && patientId == null ->
                            "Selecciona un paciente."

                        day == null ->
                            "Selecciona una fecha válida."

                        slot == null ->
                            "Selecciona un horario."

                        existing == null &&
                                slot !in available ->
                            "Selecciona un horario disponible."

                        reason.isBlank() ->
                            "Ingresa el motivo de la consulta."

                        else -> null
                    }

                    if (error == null) {
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
                modifier = Modifier.fillMaxWidth(),
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
                        text = if (existing == null) {
                            "Registrar cita"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }

            Spacer(Modifier.size(8.dp))
        }
    }

    // =====================================================
    // SELECCIÓN DE FECHA
    // =====================================================

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()

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
                state = pickerState
            )
        }
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
// SELECTOR REUTILIZABLE
// =========================================================

@Composable
private fun Selector(
    label: String,
    value: String,
    options: List<Pair<Int, String>>,
    onSelected: (Int) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box {
        OutlinedButton(
            onClick = {
                expanded = true
            },
            enabled = options.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$label: $value",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = {
                        Text(name)
                    },
                    onClick = {
                        expanded = false
                        onSelected(id)
                    }
                )
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
