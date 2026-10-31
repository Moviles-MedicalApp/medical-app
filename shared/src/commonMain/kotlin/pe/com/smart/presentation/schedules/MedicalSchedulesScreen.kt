package pe.com.smart.presentation.schedules

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule

@Composable
fun MedicalSchedulesScreen(
    onManageDoctor: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicalSchedulesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }
    val doctors = state.doctors.filter { doctor ->
        val schedules = state.schedules.filter { it.doctorId == doctor.id }
        (state.selectedDay == null || schedules.any { it.dayOfWeek == state.selectedDay }) &&
                (state.searchQuery.isBlank() || doctor.fullName.contains(state.searchQuery, true) ||
                        doctor.specialityName.contains(state.searchQuery, true) ||
                        schedules.any { it.dayOfWeek.contains(state.searchQuery, true) || it.startDisplay.contains(state.searchQuery) })
    }
    Box(modifier.fillMaxSize()) {
        when {
            state.isLoading && state.doctors.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null && state.doctors.isEmpty() -> AppError(state.error ?: "No se pudo cargar la agenda.", viewModel::load)
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    AgendaHeader(
                        state.doctors.size,
                        state.schedules.size,
                        state.doctors.count { doctor -> state.schedules.none { it.doctorId == doctor.id } },
                        state.searchQuery,
                        viewModel::onSearchChange,
                        state.selectedDay,
                        viewModel::onDaySelected
                    )
                }
                if (doctors.isEmpty()) item { AppEmptyState("Sin horarios", "No se encontraron médicos u horarios con los filtros aplicados.") }
                else items(doctors, key = { it.id }) { doctor -> ScheduleDoctorCard(doctor, state.schedules.filter { it.doctorId == doctor.id }, onManageDoctor) }
            }
        }
    }
}

@Composable
private fun AgendaHeader(doctors: Int, schedules: Int, withoutAvailability: Int, query: String, onQuery: (String) -> Unit, selectedDay: String?, onDay: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Resumen de horarios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("$doctors médicos · $schedules horarios", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (withoutAvailability > 0) {
            Text(
                "$withoutAvailability ${if (withoutAvailability == 1) "médico sin disponibilidad configurada" else "médicos sin disponibilidad configurada"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Buscar médico o horario") }, leadingIcon = { Icon(Icons.Outlined.Search, null) })
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selectedDay == null, { onDay(null) }, { Text("Todos") }, colors = scheduleFilterChipColors())
            weekDays.forEach { day -> FilterChip(selectedDay == day, { onDay(day) }, { Text(dayLabels.getValue(day)) }, colors = scheduleFilterChipColors()) }
        }
    }
}

@Composable
private fun ScheduleDoctorCard(doctor: Doctor, schedules: List<MedicalSchedule>, onManage: (Int) -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable { onManage(doctor.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(42.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(doctor.name.take(1) + doctor.lastName.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(doctor.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(doctor.specialityName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = "Gestionar horarios", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${schedules.size} horarios · ${schedules.sumOf { it.hours() }} h semanales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (schedules.isEmpty()) {
                Text("Sin disponibilidad configurada", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val scheduleGroups = schedules.groupBy { it.dayOfWeek }
                    .toList()
                    .sortedBy { weekDays.indexOf(it.first) }
                val visibleGroups = scheduleGroups.take(3)
                visibleGroups.forEach { (day, ranges) ->
                    val rangesText = ranges.sortedBy { it.startTime }.joinToString(" · ") { "${it.startDisplay}–${it.endDisplay}" }
                    Text("${dayLabels[day]} · $rangesText", style = MaterialTheme.typography.bodyMedium)
                }
                if (scheduleGroups.size > visibleGroups.size) Text("Ver los ${schedules.size} horarios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
internal fun scheduleFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
)
