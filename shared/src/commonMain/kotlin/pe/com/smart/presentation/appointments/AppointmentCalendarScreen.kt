
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.domain.model.Appointment
import pe.com.smart.presentation.schedules.scheduleFilterChipColors

private enum class CalendarViewMode {
    MONTH,
    WEEK,
    DAY
}

/**
 * Calendario médico.
 *
 * Incluye vistas mensual, semanal y diaria.
 * Utiliza el AppointmentsViewModel existente.
 */
@Composable
fun AppointmentCalendarScreen(
    onNewAppointment: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val claims = AccessTokenClaimsDecoder.decode(
        TokenStorage.getToken().orEmpty()
    )

    val isAdmin = claims?.hasRole("ADMIN") == true
    val isDoctor = !isAdmin &&
            claims?.hasRole("DOCTOR") == true
    val isPatient = !isAdmin &&
            !isDoctor &&
            claims?.hasRole("PATIENT") == true

    val canCreate = isAdmin || isPatient

    val today = remember {
        calendarTodayIso()
    }

    var selectedDate by remember {
        mutableStateOf(today)
    }

    var viewMode by remember {
        mutableStateOf(CalendarViewMode.MONTH)
    }

    var selectedStatus by remember {
        mutableStateOf<String?>(null)
    }

    var selectedDoctor by remember {
        mutableStateOf<Int?>(null)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showFilters by remember {
        mutableStateOf(false)
    }

    var detail by remember {
        mutableStateOf<Appointment?>(null)
    }

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    val visibleAppointments = state.appointments
        .filter {
            (selectedStatus == null ||
                    it.status == selectedStatus) &&
                    (selectedDoctor == null ||
                            it.doctorId == selectedDoctor)
        }
        .sortedWith(
            compareBy<Appointment> {
                it.appointmentDate
            }.thenBy {
                it.startTime
            }
        )

    val dailyAppointments = visibleAppointments.filter {
        it.appointmentDate == selectedDate
    }

    val week = weekDates(selectedDate)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading &&
                    state.appointments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null &&
                    state.appointments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AppError(
                        message = state.error
                            ?: "No se pudo cargar el calendario.",
                        onRetry = {
                            viewModel.load()
                        }
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        CalendarHeader(
                            selectedDate = selectedDate,
                            viewMode = viewMode,
                            activeFilters = listOfNotNull(
                                selectedDoctor,
                                selectedStatus
                            ).size,
                            onPrevious = {
                                selectedDate = shiftCalendarDate(
                                    selectedDate,
                                    viewMode,
                                    -1
                                )
                            },
                            onNext = {
                                selectedDate = shiftCalendarDate(
                                    selectedDate,
                                    viewMode,
                                    1
                                )
                            },
                            onDatePicker = {
                                showDatePicker = true
                            },
                            onFilters = {
                                showFilters = true
                            },
                            onViewMode = {
                                viewMode = it
                            },
                            onToday = {
                                selectedDate = today
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
                                    text = "Actualizando agenda...",
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
                                    color = MaterialTheme
                                        .colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    when (viewMode) {
                        CalendarViewMode.MONTH -> {
                            item {
                                MonthlyCalendar(
                                    selectedDate = selectedDate,
                                    today = today,
                                    appointments = visibleAppointments,
                                    onDateSelected = {
                                        selectedDate = it
                                    }
                                )
                            }
                        }

                        CalendarViewMode.WEEK,
                        CalendarViewMode.DAY -> {
                            item {
                                WeeklyCalendar(
                                    week = week,
                                    selectedDate = selectedDate,
                                    today = today,
                                    appointments = visibleAppointments,
                                    onDateSelected = {
                                        selectedDate = it
                                        viewMode = CalendarViewMode.DAY
                                    }
                                )
                            }
                        }
                    }

                    if (viewMode == CalendarViewMode.WEEK) {
                        val weeklyAppointments =
                            visibleAppointments.filter {
                                it.appointmentDate in week
                            }

                        item {
                            CalendarAgendaTitle(
                                title = "Citas de la semana",
                                subtitle =
                                    "${weeklyAppointments.size} atenciones · ${friendlyWeekRange(week)}"
                            )
                        }

                        if (weeklyAppointments.isEmpty()) {
                            item {
                                AppEmptyState(
                                    title = "Sin citas esta semana",
                                    message =
                                        "No se encontraron atenciones para este período."
                                )
                            }
                        } else {
                            week.forEach { day ->
                                val appointmentsForDay =
                                    weeklyAppointments.filter {
                                        it.appointmentDate == day
                                    }

                                if (appointmentsForDay.isNotEmpty()) {
                                    item(key = "day_$day") {
                                        Text(
                                            text = friendlyCalendarDate(day),
                                            style = MaterialTheme
                                                .typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    items(
                                        appointmentsForDay,
                                        key = { it.id }
                                    ) { appointment ->
                                        CalendarAppointmentCard(
                                            appointment = appointment,
                                            isPatient = isPatient,
                                            onClick = {
                                                detail = appointment
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            CalendarAgendaTitle(
                                title = "Agenda del día",
                                subtitle =
                                    "${friendlyCalendarDate(selectedDate)} · ${dailyAppointments.size} citas"
                            )
                        }

                        if (dailyAppointments.isEmpty()) {
                            item {
                                AppEmptyState(
                                    title = "Sin citas para esta fecha",
                                    message =
                                        "No hay atenciones que coincidan con los filtros."
                                )
                            }
                        } else {
                            items(
                                dailyAppointments,
                                key = { it.id }
                            ) { appointment ->
                                CalendarAppointmentCard(
                                    appointment = appointment,
                                    isPatient = isPatient,
                                    onClick = {
                                        detail = appointment
                                    }
                                )
                            }
                        }
                    }
                }

                if (canCreate) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Button(
                            onClick = {
                                onNewAppointment(selectedDate)
                            },
                            enabled = selectedDate >= today,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Text("Nueva cita")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        CalendarDatePicker(
            initialDate = selectedDate,
            onDismiss = {
                showDatePicker = false
            },
            onSelected = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }

    if (showFilters) {
        CalendarFilters(
            doctors = state.doctors.map {
                it.id to it.fullName
            },
            selectedDoctor = selectedDoctor,
            selectedStatus = selectedStatus,
            showDoctorFilter = isAdmin,
            onDoctor = {
                selectedDoctor = it
            },
            onStatus = {
                selectedStatus = it
            },
            onDismiss = {
                showFilters = false
            }
        )
    }

    detail?.let { appointment ->
        CalendarDetailSheet(
            appointment = appointment,
            isPatient = isPatient,
            onDismiss = {
                detail = null
            }
        )
    }
}

/**
 * Encabezado y controles del calendario.
 */
@Composable
private fun CalendarHeader(
    selectedDate: String,
    viewMode: CalendarViewMode,
    activeFilters: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDatePicker: () -> Unit,
    onFilters: () -> Unit,
    onViewMode: (CalendarViewMode) -> Unit,
    onToday: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Calendario médico",
                    style = MaterialTheme
                        .typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Text(
                    text = "Organiza y consulta las atenciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onFilters
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription =
                        "Filtrar calendario",
                    tint = colors.primary
                )
            }

            if (activeFilters > 0) {
                Text(
                    text = activeFilters.toString(),
                    color = colors.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Outlined.ChevronLeft,
                        contentDescription = "Anterior"
                    )
                }

                OutlinedButton(
                    onClick = onDatePicker,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = when (viewMode) {
                            CalendarViewMode.MONTH ->
                                calendarMonthTitle(selectedDate)

                            CalendarViewMode.WEEK ->
                                friendlyWeekRange(
                                    weekDates(selectedDate)
                                )

                            CalendarViewMode.DAY ->
                                friendlyCalendarDate(selectedDate)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = "Siguiente"
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewMode == CalendarViewMode.MONTH,
                onClick = {
                    onViewMode(CalendarViewMode.MONTH)
                },
                label = { Text("Mes") },
                colors = scheduleFilterChipColors()
            )

            FilterChip(
                selected = viewMode == CalendarViewMode.WEEK,
                onClick = {
                    onViewMode(CalendarViewMode.WEEK)
                },
                label = { Text("Semana") },
                colors = scheduleFilterChipColors()
            )

            FilterChip(
                selected = viewMode == CalendarViewMode.DAY,
                onClick = {
                    onViewMode(CalendarViewMode.DAY)
                },
                label = { Text("Día") },
                colors = scheduleFilterChipColors()
            )

            OutlinedButton(
                onClick = onToday
            ) {
                Text("Hoy")
            }
        }
    }
}

/**
 * Calendario mensual.
 */
@Composable
private fun MonthlyCalendar(
    selectedDate: String,
    today: String,
    appointments: List<Appointment>,
    onDateSelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val monthStart = selectedDate.take(7) + "-01"
    val monthDays = daysInMonth(selectedDate)

    val leadingDays = dayOfWeekMonday(monthStart)

    val cellCount =
        ((leadingDays + monthDays + 6) / 7) * 7

    Card(
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "L", "M", "M", "J", "V", "S", "D"
                ).forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme
                                .typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            repeat(cellCount / 7) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    repeat(7) { columnIndex ->
                        val cellIndex =
                            rowIndex * 7 + columnIndex

                        val day = cellIndex - leadingDays + 1

                        if (day in 1..monthDays) {
                            val date =
                                selectedDate.take(7) +
                                        "-" +
                                        day.toString()
                                            .padStart(2, '0')

                            val count = appointments.count {
                                it.appointmentDate == date
                            }

                            val selected =
                                date == selectedDate

                            val isToday = date == today

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onDateSelected(date)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    selected ->
                                        colors.primaryContainer

                                    isToday ->
                                        colors.surfaceVariant

                                    else ->
                                        colors.surface
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        vertical = 8.dp
                                    ),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontWeight =
                                            if (selected || isToday) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        color =
                                            if (selected) {
                                                colors.primary
                                            } else {
                                                colors.onSurface
                                            },
                                        style = MaterialTheme
                                            .typography.bodySmall
                                    )

                                    Box(
                                        modifier = Modifier.size(5.dp),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {
                                        if (count > 0) {
                                            Surface(
                                                modifier =
                                                    Modifier.size(5.dp),
                                                shape =
                                                    RoundedCornerShape(50),
                                                color = colors.primary
                                            ) {}
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Los puntos indican fechas con citas.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * Selector de días de la semana.
 */
@Composable
private fun WeeklyCalendar(
    week: List<String>,
    selectedDate: String,
    today: String,
    appointments: List<Appointment>,
    onDateSelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
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
                .padding(10.dp),
            horizontalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            week.forEach { date ->
                val selected = date == selectedDate

                val count = appointments.count {
                    it.appointmentDate == date
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDateSelected(date)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        selected ->
                            colors.primaryContainer

                        date == today ->
                            colors.surfaceVariant

                        else ->
                            colors.surface
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(
                            vertical = 10.dp
                        ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = shortDay(date),
                            style = MaterialTheme
                                .typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )

                        Text(
                            text = date.takeLast(2),
                            fontWeight = FontWeight.Bold,
                            color = if (selected) {
                                colors.primary
                            } else {
                                colors.onSurface
                            }
                        )

                        Text(
                            text = if (count == 0) {
                                "·"
                            } else {
                                count.toString()
                            },
                            style = MaterialTheme
                                .typography.labelSmall,
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarAgendaTitle(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tarjeta de una cita.
 */
@Composable
private fun CalendarAppointmentCard(
    appointment: Appointment,
    isPatient: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = colors.primaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = appointment.timeRange,
                        style = MaterialTheme
                            .typography.titleSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = appointment.specialityName
                            .ifBlank { "Consulta médica" },
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (isPatient) {
                    appointment.doctorFullName
                } else {
                    appointment.patientFullName
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isPatient) {
                    "Médico asignado"
                } else {
                    appointment.doctorFullName
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            CalendarStatus(appointment.status)
        }
    }
}

@Composable
private fun CalendarStatus(
    status: String
) {
    val colors = MaterialTheme.colorScheme

    val label: String
    val background: Color
    val foreground: Color

    when (status) {
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
        shape = RoundedCornerShape(8.dp),
        color = background
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = foreground
        )
    }
}

/**
 * Selector de fecha.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDatePicker(
    initialDate: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis =
            isoToEpoch(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    picker.selectedDateMillis?.let {
                        onSelected(epochToIso(it))
                    }
                },
                enabled =
                    picker.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = picker
        )
    }
}

/**
 * Filtros desplazables.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarFilters(
    doctors: List<Pair<Int, String>>,
    selectedDoctor: Int?,
    selectedStatus: String?,
    showDoctorFilter: Boolean,
    onDoctor: (Int?) -> Unit,
    onStatus: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Filtrar calendario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Estado de la cita",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    null,
                    "SCHEDULED",
                    "CONFIRMED",
                    "COMPLETED",
                    "CANCELLED"
                ).forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            onStatus(status)
                        },
                        label = {
                            Text(
                                if (status == null) {
                                    "Todos"
                                } else {
                                    calendarStatusLabel(status)
                                }
                            )
                        },
                        colors = scheduleFilterChipColors()
                    )
                }
            }

            if (showDoctorFilter) {
                Text(
                    text = "Médico",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                FilterChip(
                    selected = selectedDoctor == null,
                    onClick = {
                        onDoctor(null)
                    },
                    label = {
                        Text("Todos los médicos")
                    },
                    colors = scheduleFilterChipColors()
                )

                doctors.forEach { (id, name) ->
                    FilterChip(
                        selected = selectedDoctor == id,
                        onClick = {
                            onDoctor(id)
                        },
                        label = {
                            Text(name)
                        },
                        colors = scheduleFilterChipColors()
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    onDoctor(null)
                    onStatus(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar filtros")
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar filtros")
            }
        }
    }
}

/**
 * Detalle informativo de una cita.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CalendarDetailSheet(
    appointment: Appointment,
    isPatient: Boolean,
    onDismiss: () -> Unit
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
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detalle de la cita",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = friendlyCalendarDate(
                    appointment.appointmentDate
                ) + " · " + appointment.timeRange,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall
            )

            CalendarStatus(appointment.status)

            HorizontalDivider()

            if (!isPatient) {
                CalendarDetailRow(
                    "Paciente",
                    appointment.patientFullName
                )
            }

            CalendarDetailRow(
                "Médico",
                appointment.doctorFullName
            )

            CalendarDetailRow(
                "Especialidad",
                appointment.specialityName
            )

            CalendarDetailRow(
                "Motivo de consulta",
                appointment.reason
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar")
            }

            Spacer(Modifier.size(8.dp))
        }
    }
}

@Composable
private fun CalendarDetailRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value.ifBlank { "No especificado" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calendarStatusLabel(
    status: String
): String = when (status) {
    "SCHEDULED" -> "Programada"
    "CONFIRMED" -> "Confirmada"
    "COMPLETED" -> "Completada"
    "CANCELLED" -> "Cancelada"
    else -> status
}

/**
 * Obtiene la fecha local actual.
 *
 * Compatible con kotlinx-datetime 0.6.2.
 */
private fun calendarTodayIso(): String {
    val instant = Instant.fromEpochMilliseconds(
        kotlin.time.Clock.System.now()
            .toEpochMilliseconds()
    )

    return instant.toLocalDateTime(
        TimeZone.currentSystemDefault()
    ).date.toString()
}

private fun shiftCalendarDate(
    date: String,
    mode: CalendarViewMode,
    direction: Int
): String {
    return when (mode) {
        CalendarViewMode.DAY ->
            addDays(date, direction)

        CalendarViewMode.WEEK ->
            addDays(date, direction * 7)

        CalendarViewMode.MONTH ->
            addMonths(date, direction)
    }
}

private fun addMonths(
    date: String,
    delta: Int
): String {
    val parts = date.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()

    val total = year * 12 + month - 1 + delta
    val newYear = MathFloorDiv(total, 12)
    val newMonth = total - newYear * 12 + 1

    val day = parts[2].toInt().coerceAtMost(
        daysInMonth(
            "$newYear-${pad(newMonth.toLong())}-01"
        )
    )

    return "$newYear-${pad(newMonth.toLong())}-${pad(day.toLong())}"
}

private fun MathFloorDiv(
    value: Int,
    divisor: Int
): Int {
    return if (value >= 0) {
        value / divisor
    } else {
        (value - divisor + 1) / divisor
    }
}

private fun daysInMonth(
    date: String
): Int {
    val parts = date.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()

    val leap = year % 400 == 0 ||
            (year % 4 == 0 && year % 100 != 0)

    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (leap) 29 else 28
        else -> 30
    }
}

/**
 * 0 = lunes
 * 6 = domingo
 */
private fun dayOfWeekMonday(
    date: String
): Int {
    val epochDay =
        isoToEpoch(date) / 86_400_000L

    return (((epochDay + 3L) % 7L + 7L) % 7L)
        .toInt()
}

private fun shortDay(
    date: String
): String = listOf(
    "LUN",
    "MAR",
    "MIÉ",
    "JUE",
    "VIE",
    "SÁB",
    "DOM"
)[dayOfWeekMonday(date)]

private fun weekDates(
    date: String
): List<String> {
    val offset = dayOfWeekMonday(date)

    return (0..6).map {
        addDays(date, it - offset)
    }
}

private fun addDays(
    date: String,
    delta: Int
): String {
    return epochToIso(
        isoToEpoch(date) +
                delta * 86_400_000L
    )
}

private fun isoToEpoch(
    date: String
): Long {
    val parts = date.split("-")

    var year = parts[0].toLong()
    val month = parts[1].toLong()
    val day = parts[2].toLong()

    year -= if (month <= 2) 1 else 0

    val era = if (year >= 0) {
        year / 400
    } else {
        (year - 399) / 400
    }

    val yoe = year - era * 400

    val mp = month +
            if (month > 2) -3 else 9

    val doy = (153 * mp + 2) / 5 +
            day - 1

    val doe = yoe * 365 +
            yoe / 4 -
            yoe / 100 +
            doy

    return (
            era * 146097 +
                    doe -
                    719468
            ) * 86_400_000L
}

private fun epochToIso(
    millis: Long
): String {
    val z = millis / 86_400_000L + 719468L

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

    val mp = (5L * doy + 2L) / 153L

    val day = doy -
            (153L * mp + 2L) / 5L + 1L

    val month = mp +
            if (mp < 10L) 3L else -9L

    if (month <= 2L) {
        year++
    }

    return "$year-${pad(month)}-${pad(day)}"
}

private fun pad(
    value: Long
): String {
    return value.toString().padStart(2, '0')
}

private val calendarMonths = listOf(
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

private fun calendarMonthTitle(
    date: String
): String {
    val parts = date.split("-")
    val month = parts[1].toInt()
    val year = parts[0]

    val monthName =
        calendarMonths[month - 1]

    return monthName.replaceFirstChar {
        it.uppercase()
    } + " $year"
}

private fun friendlyCalendarDate(
    date: String
): String {
    val parts = date.split("-")

    val day = parts[2].toInt()
    val month = parts[1].toInt()
    val year = parts[0]

    return "$day de ${calendarMonths[month - 1]} de $year"
}

private fun friendlyWeekRange(
    week: List<String>
): String {
    val first = week.first()
    val last = week.last()

    val firstParts = first.split("-")
    val lastParts = last.split("-")

    val firstDay = firstParts[2].toInt()
    val lastDay = lastParts[2].toInt()

    val firstMonth =
        calendarMonths[firstParts[1].toInt() - 1]

    val lastMonth =
        calendarMonths[lastParts[1].toInt() - 1]

    return if (
        firstParts[1] == lastParts[1] &&
        firstParts[0] == lastParts[0]
    ) {
        "$firstDay–$lastDay de $firstMonth"
    } else {
        "$firstDay de $firstMonth – $lastDay de $lastMonth"
    }
}
