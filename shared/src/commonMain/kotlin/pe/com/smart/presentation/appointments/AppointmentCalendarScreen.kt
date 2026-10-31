package pe.com.smart.presentation.appointments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.domain.model.Appointment
import pe.com.smart.presentation.schedules.scheduleFilterChipColors

@Composable
fun AppointmentCalendarScreen(
    onNewAppointment: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedDoctor by remember { mutableStateOf<Int?>(null) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.WEEK) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Appointment?>(null) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.appointments) {
        if (selectedDate == null) selectedDate =
            state.appointments.filter { it.status == "SCHEDULED" || it.status == "CONFIRMED" }
                .minByOrNull { it.appointmentDate }?.appointmentDate
                ?: state.appointments.minByOrNull { it.appointmentDate }?.appointmentDate
    }
    val date = selectedDate
    when {
        state.isLoading && state.appointments.isEmpty() -> Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.error != null && state.appointments.isEmpty() -> AppError(
            state.error ?: "No se pudo cargar el calendario.", viewModel::load
        )

        date != null -> {
            val week = weekDates(date)
            val visibleAppointments = state.appointments
                .filter { (selectedStatus == null || it.status == selectedStatus) && (selectedDoctor == null || it.doctorId == selectedDoctor) }
                .sortedWith(compareBy<Appointment> { it.appointmentDate }.thenBy { it.startTime })
            val daily = visibleAppointments.filter { it.appointmentDate == date }
            Column(modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        CalendarControls(
                            date = date,
                            week = week,
                            appointments = visibleAppointments,
                            viewMode = viewMode,
                            onPrevious = { selectedDate = addDays(date, -7) },
                            onNext = { selectedDate = addDays(date, 7) },
                            onDatePicker = { showDatePicker = true },
                            onFilters = { showFilters = true },
                            onDate = { selectedDate = it; viewMode = CalendarViewMode.DAY },
                            onViewMode = { viewMode = it }
                        )
                    }
                    if (viewMode == CalendarViewMode.WEEK) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Citas de la semana",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${visibleAppointments.count { it.appointmentDate in week }} citas · ${
                                        friendlyWeekRange(
                                            week
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (week.none { value -> visibleAppointments.any { it.appointmentDate == value } }) {
                            item {
                                AppEmptyState(
                                    "Sin citas esta semana",
                                    "No hay atenciones que coincidan con los filtros."
                                )
                            }
                        } else {
                            val daysWithAppointments =
                                week.filter { value -> visibleAppointments.any { it.appointmentDate == value } }
                            daysWithAppointments.forEach { value ->
                                val appointmentsForDay =
                                    visibleAppointments.filter { it.appointmentDate == value }
                                item(key = "day-$value") {
                                    WeekDaySection(
                                        date = value,
                                        appointments = appointmentsForDay,
                                        onSelect = { selectedDate = value },
                                        onDetail = { detail = it }
                                    )
                                }
                            }
                            val emptyDays = week.size - daysWithAppointments.size
                            if (emptyDays > 0) item {
                                Text(
                                    "$emptyDays ${if (emptyDays == 1) "día sin citas" else "días sin citas"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                "Agenda del ${friendlyCalendarDate(date)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (daily.isEmpty()) item {
                            AppEmptyState(
                                "Sin citas para este día",
                                "No hay atenciones que coincidan con los filtros."
                            )
                        }
                        else items(daily, key = { it.id }) { appointment ->
                            CalendarAppointmentCard(
                                appointment
                            ) { detail = appointment }
                        }
                    }
                }
                Surface(shadowElevation = 4.dp) {
                    Button(
                        onClick = onNewAppointment,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    ) { Text("Nueva cita") }
                }
            }
        }
    }
    if (showDatePicker) CalendarDatePicker(onDismiss = { showDatePicker = false }) {
        selectedDate = it; showDatePicker = false
    }
    if (showFilters) CalendarFilters(
        state.doctors.map { it.id to it.fullName },
        selectedDoctor,
        selectedStatus,
        onDoctor = { selectedDoctor = it },
        onStatus = { selectedStatus = it },
        onDismiss = { showFilters = false })
    detail?.let { CalendarDetailSheet(it, onDismiss = { detail = null }) }
}

@Composable
private fun CalendarControls(
    date: String,
    week: List<String>,
    appointments: List<Appointment>,
    viewMode: CalendarViewMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDatePicker: () -> Unit,
    onFilters: () -> Unit,
    onDate: (String) -> Unit,
    onViewMode: (CalendarViewMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) { Icon(Icons.Outlined.ChevronLeft, "Semana anterior") }
            OutlinedButton(onClick = onDatePicker, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.CalendarMonth, null)
                Text(
                    " ${
                        if (viewMode == CalendarViewMode.WEEK) friendlyWeekRange(week) else friendlyCalendarDate(
                            date
                        )
                    }"
                )
            }
            IconButton(onClick = onNext) { Icon(Icons.Outlined.ChevronRight, "Semana siguiente") }
            IconButton(onClick = onFilters) { Icon(Icons.Outlined.FilterList, "Filtros") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = viewMode == CalendarViewMode.WEEK,
                onClick = { onViewMode(CalendarViewMode.WEEK) },
                label = { Text("Semana") },
                colors = scheduleFilterChipColors()
            )
            FilterChip(
                selected = viewMode == CalendarViewMode.DAY,
                onClick = { onViewMode(CalendarViewMode.DAY) },
                label = { Text("Día") },
                colors = scheduleFilterChipColors()
            )
        }
        if (viewMode == CalendarViewMode.DAY) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { value ->
                    val count = appointments.count {
                        it.appointmentDate == value && it.status in setOf(
                            "SCHEDULED",
                            "CONFIRMED"
                        )
                    }
                    val selected = value == date
                    Surface(
                        modifier = Modifier.weight(1f).clickable { onDate(value) },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(shortDay(value), style = MaterialTheme.typography.labelSmall)
                            Text(value.takeLast(2), fontWeight = FontWeight.Bold)
                            Text(
                                if (count == 0) "·" else count.toString(),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class CalendarViewMode { WEEK, DAY }

@Composable
private fun WeekDaySection(
    date: String,
    appointments: List<Appointment>,
    onSelect: () -> Unit,
    onDetail: (Appointment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "${shortDay(date)} · ${friendlyCalendarDate(date)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (appointments.isEmpty()) "Sin citas" else "${appointments.size} ${if (appointments.size == 1) "cita" else "citas"} · ${
                            appointments.first().startTime.take(
                                5
                            )
                        }–${appointments.last().endTime.take(5)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            appointments.forEach { appointment ->
                CalendarAppointmentCard(appointment) { onDetail(appointment) }
            }
        }
    }
}

@Composable
private fun CalendarAppointmentCard(item: Appointment, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${item.startTime.take(5)}–${item.endTime.take(5)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    item.patientFullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${item.doctorFullName} · ${item.specialityName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CalendarStatus(item.status)
        }
    }
}

@Composable
private fun CalendarStatus(status: String) = Surface(
    shape = MaterialTheme.shapes.small,
    color = if (status == "COMPLETED") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
) {
    Text(
        calendarStatusLabel(status),
        Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDatePicker(onDismiss: () -> Unit, onSelected: (String) -> Unit) {
    val picker = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                picker.selectedDateMillis?.let {
                    onSelected(
                        epochToIso(it)
                    )
                }
            }) { Text("Aceptar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }) {
        DatePicker(
            picker
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarFilters(
    doctors: List<Pair<Int, String>>,
    doctor: Int?,
    status: String?,
    onDoctor: (Int?) -> Unit,
    onStatus: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Filtrar agenda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Estado", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    status == null,
                    { onStatus(null) },
                    { Text("Todos") },
                    colors = scheduleFilterChipColors()
                ); listOf(
                "SCHEDULED",
                "CONFIRMED",
                "COMPLETED"
            ).forEach { value ->
                FilterChip(
                    status == value,
                    { onStatus(value) },
                    { Text(calendarStatusLabel(value)) },
                    colors = scheduleFilterChipColors()
                )
            }
            }
            Text("Médico", style = MaterialTheme.typography.titleSmall)
            FilterChip(
                doctor == null,
                { onDoctor(null) },
                { Text("Todos los médicos") },
                colors = scheduleFilterChipColors()
            )
            doctors.forEach { (id, name) ->
                FilterChip(
                    doctor == id,
                    { onDoctor(id) },
                    { Text(name) },
                    colors = scheduleFilterChipColors()
                )
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Listo") }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDetailSheet(item: Appointment, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                item.patientFullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${item.startTime.take(5)}–${item.endTime.take(5)} · ${calendarStatusLabel(item.status)}",
                color = MaterialTheme.colorScheme.primary
            )
            Text(item.doctorFullName)
            Text(item.specialityName, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Motivo", style = MaterialTheme.typography.titleSmall)
            Text(item.reason)
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar") }
        }
    }
}

private fun calendarStatusLabel(value: String) = when (value) {
    "SCHEDULED" -> "Programada"; "CONFIRMED" -> "Confirmada"; "COMPLETED" -> "Completada"; "CANCELLED" -> "Cancelada"; else -> value
}

private fun shortDay(date: String) = when (dayOfWeek(date)) {
    0 -> "DOM"; 1 -> "LUN"; 2 -> "MAR"; 3 -> "MIÉ"; 4 -> "JUE"; 5 -> "VIE"; else -> "SÁB"
}

private fun weekDates(date: String): List<String> {
    val offset = dayOfWeek(date); return (-offset..6 - offset).map { addDays(date, it) }
}

private fun dayOfWeek(date: String): Int {
    val p = date.split("-");
    val y = p[0].toInt();
    val m = p[1].toInt();
    val d = p[2].toInt();
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4);
    val yy = if (m < 3) y - 1 else y; return (yy + yy / 4 - yy / 100 + yy / 400 + t[m - 1] + d) % 7
}

private fun addDays(date: String, delta: Int): String =
    epochToIso(isoToEpoch(date) + delta * 86_400_000L)

private fun isoToEpoch(date: String): Long {
    val p = date.split("-");
    var y = p[0].toLong();
    val m = p[1].toLong();
    val d = p[2].toLong(); y -= if (m <= 2) 1 else 0;
    val era = if (y >= 0) y / 400 else (y - 399) / 400;
    val yoe = y - era * 400;
    val mp = m + if (m > 2) -3 else 9;
    val doy = (153 * mp + 2) / 5 + d - 1;
    val doe =
        yoe * 365 + yoe / 4 - yoe / 100 + doy; return (era * 146097 + doe - 719468) * 86_400_000L
}

private fun epochToIso(millis: Long): String {
    val z = millis / 86_400_000L + 719468L;
    val era = if (z >= 0) z / 146097L else (z - 146096L) / 146097L;
    val doe = z - era * 146097L;
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L;
    var y = yoe + era * 400L;
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L);
    val mp = (5L * doy + 2L) / 153L;
    val d = doy - (153L * mp + 2L) / 5L + 1L;
    val m = mp + if (mp < 10L) 3 else -9; if (m <= 2) y++; return "$y-${pad(m)}-${pad(d)}"
}

private fun pad(value: Long) = if (value < 10) "0$value" else value.toString()
private fun friendlyCalendarDate(date: String): String {
    val p = date.split("-");
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
    ); return "${p[2].toInt()} de ${months[p[1].toInt() - 1]}"
}

private fun friendlyWeekRange(week: List<String>): String {
    val first = week.first().split("-")
    val last = week.last().split("-")
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
    val firstMonth = months[first[1].toInt() - 1]
    val lastMonth = months[last[1].toInt() - 1]
    return if (firstMonth == lastMonth) "${first[2].toInt()}–${last[2].toInt()} de $firstMonth" else "${first[2].toInt()} de $firstMonth – ${last[2].toInt()} de $lastMonth"
}
