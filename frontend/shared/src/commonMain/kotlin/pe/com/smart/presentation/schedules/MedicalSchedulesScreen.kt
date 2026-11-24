
package pe.com.smart.presentation.schedules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.presentation.components.form.AppSearchField
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule

@Composable
fun MedicalSchedulesScreen(
    onManageDoctor: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicalSchedulesViewModel = koinViewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    val schedulesByDoctor = state.schedules.groupBy {
        it.doctorId
    }

    val filteredDoctors = state.doctors.filter { doctor ->
        val schedules = schedulesByDoctor[doctor.id].orEmpty()

        val matchesDay =
            state.selectedDay == null ||
                    schedules.any {
                        it.dayOfWeek == state.selectedDay
                    }

        val query = state.searchQuery.trim()

        val matchesSearch = query.isBlank() ||
                doctor.fullName.contains(query, ignoreCase = true) ||
                doctor.specialityName.contains(
                    query,
                    ignoreCase = true
                ) ||
                schedules.any { schedule ->
                    schedule.dayOfWeek.contains(
                        query,
                        ignoreCase = true
                    ) ||
                            dayLabels[schedule.dayOfWeek]
                                .orEmpty()
                                .contains(query, ignoreCase = true) ||
                            schedule.startDisplay.contains(query) ||
                            schedule.endDisplay.contains(query)
                }

        matchesDay && matchesSearch
    }

    val withoutAvailability = state.doctors.count { doctor ->
        schedulesByDoctor[doctor.id].isNullOrEmpty()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading && state.doctors.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.doctors.isEmpty() -> {
                AppError(
                    message = state.error
                        ?: "No se pudieron cargar los horarios.",
                    onRetry = viewModel::load
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 20.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Horarios médicos",
                                style = MaterialTheme
                                    .typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Consulta la disponibilidad semanal y administra los turnos de cada médico.",
                                style = MaterialTheme
                                    .typography.bodyMedium,
                                color = MaterialTheme
                                    .colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            ScheduleSummaryCard(
                                value = state.doctors.size.toString(),
                                label = "Médicos",
                                icon = Icons.Outlined.People,
                                modifier = Modifier.weight(1f)
                            )

                            ScheduleSummaryCard(
                                value = state.schedules.size.toString(),
                                label = "Horarios",
                                icon = Icons.Outlined.CalendarMonth,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (withoutAvailability > 0) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme
                                    .colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme
                                            .colorScheme.onErrorContainer
                                    )

                                    Text(
                                        text = "$withoutAvailability " +
                                                if (withoutAvailability == 1) {
                                                    "médico sin horarios configurados"
                                                } else {
                                                    "médicos sin horarios configurados"
                                                },
                                        style = MaterialTheme
                                            .typography.bodySmall,
                                        color = MaterialTheme
                                            .colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    item {
                        AppSearchField(
                            query = state.searchQuery,
                            onQueryChange = viewModel::onSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Nombre, especialidad o día",
                            contentDescription = "Buscar médico u horario"
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.selectedDay == null,
                                onClick = {
                                    viewModel.onDaySelected(null)
                                },
                                label = { Text("Todos") },
                                colors = scheduleFilterChipColors()
                            )

                            weekDays.forEach { day ->
                                FilterChip(
                                    selected = state.selectedDay == day,
                                    onClick = {
                                        viewModel.onDaySelected(day)
                                    },
                                    label = {
                                        Text(
                                            dayLabels[day] ?: day
                                        )
                                    },
                                    colors = scheduleFilterChipColors()
                                )
                            }
                        }
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
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )

                                Text(
                                    text = "Actualizando horarios...",
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )
                            }
                        }
                    }

                    if (!state.error.isNullOrBlank()) {
                        item {
                            Surface(
                                color = MaterialTheme
                                    .colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
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

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Disponibilidad por médico",
                                style = MaterialTheme
                                    .typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${filteredDoctors.size} médicos",
                                style = MaterialTheme
                                    .typography.labelMedium,
                                color = MaterialTheme
                                    .colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (filteredDoctors.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = "Sin resultados",
                                message = if (
                                    state.searchQuery.isNotBlank() ||
                                    state.selectedDay != null
                                ) {
                                    "No se encontraron médicos con los filtros seleccionados."
                                } else {
                                    "Todavía no hay médicos registrados."
                                }
                            )
                        }
                    } else {
                        items(
                            items = filteredDoctors,
                            key = { it.id }
                        ) { doctor ->
                            ScheduleDoctorCard(
                                doctor = doctor,
                                schedules = schedulesByDoctor[
                                    doctor.id
                                ].orEmpty(),
                                onClick = {
                                    onManageDoctor(doctor.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSummaryCard(
    value: String,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(23.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
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

@Composable
private fun ScheduleDoctorCard(
    doctor: Doctor,
    schedules: List<MedicalSchedule>,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val groups = schedules
        .groupBy { it.dayOfWeek }
        .toList()
        .sortedBy { (day, _) ->
            scheduleDayOrder(day)
        }

    val totalMinutes = schedules.sumOf {
        scheduleDurationMinutes(it)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scheduleInitials(
                                doctor.fullName
                            ),
                            color = colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = doctor.fullName,
                        style = MaterialTheme
                            .typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = doctor.specialityName,
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.primary
                    )
                }

                Icon(
                    imageVector =
                        Icons.Outlined.ChevronRight,
                    contentDescription =
                        "Gestionar horarios",
                    tint = colors.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = colors.primary
                )

                Text(
                    text = "${schedules.size} bloques · " +
                            formatScheduleHours(totalMinutes) +
                            " semanales",
                    style = MaterialTheme
                        .typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            if (schedules.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.errorContainer
                ) {
                    Text(
                        text = "Sin disponibilidad configurada",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp
                        ),
                        style = MaterialTheme
                            .typography.labelSmall,
                        color = colors.onErrorContainer
                    )
                }
            } else {
                groups.take(3).forEach { (day, ranges) ->
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = dayLabels[day] ?: day,
                            style = MaterialTheme
                                .typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = ranges
                                .sortedBy { it.startTime }
                                .joinToString(" · ") {
                                    "${it.startDisplay}–${it.endDisplay}"
                                },
                            style = MaterialTheme
                                .typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                if (groups.size > 3) {
                    Text(
                        text = "Ver todos los horarios",
                        style = MaterialTheme
                            .typography.labelMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun scheduleInitials(name: String): String {
    val words = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        words.isEmpty() -> "M"
        words.size == 1 ->
            words.first().take(2).uppercase()
        else -> "${words.first().first()}${words.last().first()}"
            .uppercase()
    }
}

/**
 * Orden visual de lunes a domingo.
 * No modifica los códigos usados por el backend.
 */
private fun scheduleDayOrder(day: String): Int =
    when (day) {
        "MONDAY" -> 0
        "TUESDAY" -> 1
        "WEDNESDAY" -> 2
        "THURSDAY" -> 3
        "FRIDAY" -> 4
        "SATURDAY" -> 5
        "SUNDAY" -> 6
        else -> 7
    }

private fun scheduleDurationMinutes(
    schedule: MedicalSchedule
): Int {
    fun minutes(value: String): Int? {
        val parts = value.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()
        val minute = parts.getOrNull(1)?.toIntOrNull()
        return if (hour != null && minute != null) {
            hour * 60 + minute
        } else {
            null
        }
    }

    val start = minutes(schedule.startTime) ?: return 0
    val end = minutes(schedule.endTime) ?: return 0

    return (end - start).coerceAtLeast(0)
}

private fun formatScheduleHours(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60

    return if (remaining == 0) {
        "$hours h"
    } else {
        "$hours h $remaining min"
    }
}

/**
 * Utilizado también por las demás pantallas
 * de horarios y el calendario médico.
 */
@Composable
internal fun scheduleFilterChipColors() =
    FilterChipDefaults.filterChipColors(
        selectedContainerColor =
            MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor =
            MaterialTheme.colorScheme.onPrimaryContainer
    )
