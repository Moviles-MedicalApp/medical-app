package pe.com.smart.presentation.appointments

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.domain.model.Patient
import pe.com.smart.presentation.schedules.scheduleFilterChipColors

@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    initialCreate: Boolean = false,
    initialPatientId: Int? = null,
    initialDate: String? = null,
    onInitialCreateDismiss: () -> Unit = {},
    viewModel: AppointmentsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var listScope by remember { mutableStateOf(AppointmentListScope.ACTIVE) }
    var editor by remember { mutableStateOf<Appointment?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var cancelling by remember { mutableStateOf<Appointment?>(null) }
    var detail by remember { mutableStateOf<Appointment?>(null) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(initialCreate) {
        if (initialCreate) {
            editor = null
            showEditor = true
        }
    }
    val filtered = state.appointments.filter {
        val belongsToScope = when (listScope) {
            AppointmentListScope.ACTIVE -> it.status in setOf("SCHEDULED", "CONFIRMED")
            AppointmentListScope.HISTORY -> it.status in setOf("COMPLETED", "CANCELLED")
            AppointmentListScope.ALL -> true
        }
        belongsToScope && (status == null || it.status == status) && listOf(it.patientFullName, it.patientDni, it.doctorFullName, it.specialityName, it.reason, it.appointmentDate).any { value -> value.contains(query, true) }
    }
    when {
        state.isLoading && state.appointments.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null && state.appointments.isEmpty() -> AppError(state.error ?: "No se pudieron cargar las citas.", viewModel::load)
        else -> LazyColumn(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            item {
                AppointmentHeader(state.appointments, query, { query = it }, status, { status = it }, listScope, { listScope = it; status = null }, onNew = { editor = null; showEditor = true })
            }
            if (filtered.isEmpty()) item { AppEmptyState(if (listScope == AppointmentListScope.ACTIVE) "Sin citas activas" else "Sin citas", "No hay citas que coincidan con los filtros.") }
            else {
                val sorted = filtered.sortedWith(appointmentOperationalComparator)
                sorted.groupBy { it.appointmentDate }.forEach { (date, dailyAppointments) ->
                    if (dailyAppointments.size > 1) item(key = "date_$date") { Text(friendlyDate(date), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(dailyAppointments, key = { it.id }) {
                        AppointmentCard(
                            it,
                            onOpen = { detail = it },
                            onEdit = { editor = it; showEditor = true },
                            onConfirm = { viewModel.changeStatus(it, "confirm") },
                            onComplete = { viewModel.changeStatus(it, "complete") },
                            onCancel = { cancelling = it }
                        )
                    }
                }
            }
        }
    }
    if (showEditor) AppointmentEditor(editor, state.doctors, state.patients, state.schedules, state.appointments, state.isSaving, initialPatientId, initialDate, {
        showEditor = false
        if (initialCreate) onInitialCreateDismiss()
    }) { reason, date, start, end, patient, doctor ->
        viewModel.save(editor, reason, date, start, end, patient, doctor) {
            showEditor = false
            if (initialCreate) onInitialCreateDismiss()
        }
    }
    AppConfirmDialog(visible = cancelling != null, title = "Cancelar cita", message = "Se cancelará la cita de ${cancelling?.patientFullName} del ${cancelling?.appointmentDate} a las ${cancelling?.startTime?.take(5)}.", confirmText = "Cancelar cita", destructive = true, isLoading = state.isSaving, onConfirm = { cancelling?.let { viewModel.changeStatus(it, "cancel"); cancelling = null } }, onDismiss = { cancelling = null })
    detail?.let { appointment ->
        AppointmentDetailSheet(
            appointment = appointment,
            onDismiss = { detail = null },
            onEdit = { detail = null; editor = appointment; showEditor = true },
            onConfirm = { viewModel.changeStatus(appointment, "confirm"); detail = null },
            onComplete = { viewModel.changeStatus(appointment, "complete"); detail = null },
            onCancel = { detail = null; cancelling = appointment }
        )
    }
}

@Composable
private fun AppointmentHeader(all: List<Appointment>, query: String, onQuery: (String) -> Unit, selected: String?, onStatus: (String?) -> Unit, scope: AppointmentListScope, onScope: (AppointmentListScope) -> Unit, onNew: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column { Text("Agenda de citas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Gestiona y da seguimiento a las atenciones.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Button(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(" Nueva cita") }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(scope == AppointmentListScope.ACTIVE, { onScope(AppointmentListScope.ACTIVE) }, { Text("Agenda activa") }, colors = scheduleFilterChipColors())
            FilterChip(scope == AppointmentListScope.HISTORY, { onScope(AppointmentListScope.HISTORY) }, { Text("Historial") }, colors = scheduleFilterChipColors())
            FilterChip(scope == AppointmentListScope.ALL, { onScope(AppointmentListScope.ALL) }, { Text("Todas") }, colors = scheduleFilterChipColors())
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryPill("Total", all.size) { onStatus(null) }; SummaryPill("Programadas", all.count { it.status == "SCHEDULED" }) { onStatus("SCHEDULED") }; SummaryPill("Confirmadas", all.count { it.status == "CONFIRMED" }) { onStatus("CONFIRMED") }
        }
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Buscar cita") }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected == null, { onStatus(null) }, { Text("Todas (${all.size})") }, colors = scheduleFilterChipColors())
            listOf("SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED").forEach { value -> FilterChip(selected == value, { onStatus(value) }, { Text("${statusLabel(value)} (${all.count { it.status == value }})") }, colors = scheduleFilterChipColors()) }
        }
    }
}

private enum class AppointmentListScope { ACTIVE, HISTORY, ALL }

@Composable private fun SummaryPill(label: String, value: Int, onClick: () -> Unit) = Surface(modifier = Modifier.clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) { Text("$label  $value", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge) }

@Composable
private fun AppointmentCard(item: Appointment, onOpen: () -> Unit, onEdit: () -> Unit, onConfirm: () -> Unit, onComplete: () -> Unit, onCancel: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable(onClick = onOpen), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${friendlyDate(item.appointmentDate)} · ${item.timeRange}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(item.patientFullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${item.doctorFullName} · ${item.specialityName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                StatusBadge(item.status)
            }
            if (item.status == "SCHEDULED" || item.status == "CONFIRMED") Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, "Acciones") }
                DropdownMenu(expanded, { expanded = false }) {
                    if (item.status == "SCHEDULED") { DropdownMenuItem(text = { Text("Reprogramar") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { expanded = false; onEdit() }); DropdownMenuItem(text = { Text("Confirmar") }, leadingIcon = { Icon(Icons.Outlined.Check, null) }, onClick = { expanded = false; onConfirm() }) }
                    if (item.status == "CONFIRMED") DropdownMenuItem(text = { Text("Completar") }, leadingIcon = { Icon(Icons.Outlined.Check, null) }, onClick = { expanded = false; onComplete() })
                    DropdownMenuItem(text = { Text("Cancelar") }, leadingIcon = { Icon(Icons.Outlined.Close, null) }, onClick = { expanded = false; onCancel() })
                }
            }
        }
    }
}

@Composable private fun StatusBadge(status: String) {
    val (container, content) = when (status) {
        "COMPLETED" -> Color(0xFFE4F6EA) to Color(0xFF177A39)
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        "CONFIRMED" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }
    Surface(shape = MaterialTheme.shapes.small, color = container) { Text(statusLabel(status), Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = content) }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppointmentDetailSheet(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Detalle de la cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${friendlyDate(appointment.appointmentDate)} · ${appointment.timeRange}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            StatusBadge(appointment.status)
            Text(appointment.patientFullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${appointment.doctorFullName} · ${appointment.specialityName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Motivo", style = MaterialTheme.typography.labelLarge)
            Text(appointment.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
            when (appointment.status) {
                "SCHEDULED" -> {
                    Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("Confirmar cita") }
                    OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Reprogramar") }
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancelar cita") }
                }
                "CONFIRMED" -> {
                    Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Completar cita") }
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancelar cita") }
                }
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar") }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppointmentEditor(existing: Appointment?, doctors: List<Doctor>, patients: List<Patient>, schedules: List<MedicalSchedule>, appointments: List<Appointment>, saving: Boolean, initialPatientId: Int?, initialDate: String?, onDismiss: () -> Unit, onSave: (String, String, String, String, Int, Int) -> Unit) {
    var speciality by remember(existing) { mutableStateOf(existing?.specialityName ?: "") }
    var doctorId by remember(existing) { mutableStateOf(existing?.doctorId) }
    var patientId by remember(existing, initialPatientId) { mutableStateOf(existing?.patientId ?: initialPatientId) }
    var date by remember(existing, initialDate) { mutableStateOf(existing?.appointmentDate ?: initialDate.orEmpty()) }
    var slot by remember(existing) { mutableStateOf(existing?.let { it.startTime.take(5) to it.endTime.take(5) }) }
    var reason by remember(existing) { mutableStateOf(existing?.reason ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDoctor = doctors.firstOrNull { it.id == doctorId }
    val day = dayCode(date)
    val available = if (selectedDoctor != null && day != null) appointmentSlots(schedules.filter { it.doctorId == selectedDoctor.id && it.dayOfWeek == day }, appointments.filter { it.doctorId == selectedDoctor.id && it.appointmentDate == date && it.status in setOf("SCHEDULED", "CONFIRMED") && it.id != existing?.id }) else emptyList()
    LaunchedEffect(selectedDoctor?.id, date, available) {
        if (existing == null && slot == null) slot = available.firstOrNull()
    }
    ModalBottomSheet(onDismissRequest = { if (!saving) onDismiss() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(if (existing == null) "Nueva cita" else "Editar cita", style = MaterialTheme.typography.headlineSmall)
            Text("Selecciona un médico, fecha, horario y paciente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            EditorSection("1", "Atención") {
                Text("Especialidad", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { doctors.map { it.specialityName }.distinct().forEach { name -> FilterChip(speciality == name, { speciality = name; doctorId = null; slot = null }, { Text(name) }, colors = scheduleFilterChipColors()) } }
                Selector("Médico", selectedDoctor?.fullName ?: "Selecciona un médico", doctors.filter { speciality.isBlank() || it.specialityName == speciality }.map { it.id to it.fullName }) { doctorId = it; slot = null }
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.CalendarMonth, null)
                    Text(if (date.isBlank()) " Seleccionar fecha" else " ${friendlyDate(date)}", modifier = Modifier.weight(1f))
                }
            }
            EditorSection("2", "Horario disponible") {
                when {
                    selectedDoctor == null || day == null -> Text("Selecciona un médico y una fecha para ver los horarios.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    available.isEmpty() -> Text("No hay horarios disponibles para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> available.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { value -> FilterChip(slot == value, { slot = value }, { Text("${value.first}–${value.second}") }, colors = scheduleFilterChipColors(), modifier = Modifier.weight(1f)) }; if (row.size == 1) Box(Modifier.weight(1f)) } }
                }
                slot?.let { Text("${if (existing == null) "Horario sugerido" else "Seleccionado"}: ${friendlyDate(date)} · ${it.first}–${it.second}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            }
            EditorSection("3", "Paciente y motivo") {
                Selector("Paciente", patients.firstOrNull { it.id == patientId }?.fullName ?: "Selecciona un paciente", patients.map { it.id to "${it.fullName} · ${it.dni}" }) { patientId = it }
                OutlinedTextField(reason, { if (it.length <= 500) reason = it }, Modifier.fillMaxWidth(), label = { Text("Motivo de la cita") }, supportingText = { Text("${reason.length}/500") }, minLines = 3)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                error = when { selectedDoctor == null -> "Selecciona un médico."; patientId == null -> "Selecciona un paciente."; day == null -> "Usa una fecha válida (AAAA-MM-DD)."; slot == null -> "Selecciona un horario disponible."; reason.isBlank() -> "Ingresa el motivo de la cita."; else -> null }
                if (error == null) onSave(reason.trim(), date, slot!!.first, slot!!.second, patientId!!, doctorId!!)
            }, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando..." else if (existing == null) "Registrar cita" else "Guardar cambios") }
            OutlinedButton(onClick = onDismiss, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            Box(Modifier.size(8.dp))
        }
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { Button(onClick = { pickerState.selectedDateMillis?.let { date = epochDateToIso(it); slot = null }; showDatePicker = false }) { Text("Aceptar") } }, dismissButton = { OutlinedButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun EditorSection(number: String, title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) { Text(number, Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                Text(title, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable private fun Selector(label: String, value: String, options: List<Pair<Int, String>>, onSelected: (Int) -> Unit) { var expanded by remember { mutableStateOf(false) }; Box { OutlinedButton({ expanded = true }, Modifier.fillMaxWidth(), enabled = options.isNotEmpty()) { Text("$label: $value", modifier = Modifier.weight(1f)) }; DropdownMenu(expanded, { expanded = false }) { options.forEach { (id, text) -> DropdownMenuItem({ Text(text) }, onClick = { expanded = false; onSelected(id) }) } } } }

private fun statusLabel(value: String) = when (value) { "SCHEDULED" -> "Programada"; "CONFIRMED" -> "Confirmada"; "COMPLETED" -> "Completada"; "CANCELLED" -> "Cancelada"; else -> value }
private val appointmentOperationalComparator = Comparator<Appointment> { first, second ->
    val firstHistory = first.status in setOf("COMPLETED", "CANCELLED")
    val secondHistory = second.status in setOf("COMPLETED", "CANCELLED")
    when {
        firstHistory != secondHistory -> if (firstHistory) 1 else -1
        firstHistory -> compareValues(second.appointmentDate, first.appointmentDate).takeIf { it != 0 }
            ?: compareValues(second.startTime, first.startTime)
        else -> compareValues(first.appointmentDate, second.appointmentDate).takeIf { it != 0 }
            ?: compareValues(first.startTime, second.startTime)
    }
}
private fun appointmentSlots(schedules: List<MedicalSchedule>, occupied: List<Appointment>): List<Pair<String, String>> = schedules.sortedBy { it.startTime }.flatMap { schedule -> generateSequence(schedule.startDisplay) { addMinutes(it, schedule.appointmentDuration) }.takeWhile { start -> addMinutes(start, schedule.appointmentDuration) <= schedule.endDisplay }.map { start -> start to addMinutes(start, schedule.appointmentDuration) }.filter { value -> occupied.none { it.startTime.take(5) < value.second && it.endTime.take(5) > value.first } }.toList() }
private fun addMinutes(value: String, minutes: Int): String { val total = value.take(2).toInt() * 60 + value.substring(3, 5).toInt() + minutes; return twoDigits(total / 60) + ":" + twoDigits(total % 60) }
private fun twoDigits(value: Int) = if (value < 10) "0$value" else value.toString()
private fun dayCode(date: String): String? { val p = date.split("-"); val y = p.getOrNull(0)?.toIntOrNull() ?: return null; val m = p.getOrNull(1)?.toIntOrNull() ?: return null; val d = p.getOrNull(2)?.toIntOrNull() ?: return null; if (m !in 1..12 || d !in 1..31 || date.length != 10) return null; val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4); val yy = if (m < 3) y - 1 else y; return listOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")[(yy + yy / 4 - yy / 100 + yy / 400 + t[m - 1] + d) % 7] }
private fun friendlyDate(value: String): String {
    val parts = value.split("-")
    val month = listOf("ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic.").getOrNull(parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: -1)
    return if (parts.size == 3 && month != null) "${parts[2].toIntOrNull() ?: parts[2]} de $month de ${parts[0]}" else value
}
private fun epochDateToIso(millis: Long): String {
    var z = millis / 86_400_000L + 719468L
    val era = if (z >= 0) z / 146097L else (z - 146096L) / 146097L
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    var year = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val monthPart = (5L * doy + 2L) / 153L
    val day = doy - (153L * monthPart + 2L) / 5L + 1L
    val month = monthPart + if (monthPart < 10L) 3L else -9L
    if (month <= 2L) year++
    return "$year-${twoDigits(month.toInt())}-${twoDigits(day.toInt())}"
}
