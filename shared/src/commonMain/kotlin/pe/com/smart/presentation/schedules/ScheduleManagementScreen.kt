package pe.com.smart.presentation.schedules

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule

private data class ManagementState(val doctor: Doctor? = null, val schedules: List<MedicalSchedule> = emptyList(), val loading: Boolean = false, val saving: Boolean = false, val error: String? = null)

private class ScheduleManagementViewModel : ViewModel() {
    private val _state = MutableStateFlow(ManagementState())
    val state = _state.asStateFlow()
    fun load(doctorId: Int) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        val doctor = ApiProvider.doctorRepository.getDoctorById(doctorId)
        val schedules = ApiProvider.medicalScheduleRepository.getSchedulesByDoctor(doctorId)
        _state.value = ManagementState(doctor.getOrNull(), schedules.getOrDefault(emptyList()), false, false, doctor.exceptionOrNull()?.message ?: schedules.exceptionOrNull()?.message)
    }

    fun save(schedule: MedicalSchedule?, days: Set<String>, start: String, end: String, duration: Int, doctorId: Int, onDone: () -> Unit) = viewModelScope.launch {
        _state.value = _state.value.copy(saving = true)
        if (schedule != null) {
            ApiProvider.medicalScheduleRepository.updateSchedule(schedule.id, days.first(), start, end, duration, doctorId)
                .onSuccess { UiMessageManager.success("Horario actualizado"); onDone(); load(doctorId) }
                .onFailure { UiMessageManager.error("No se pudo guardar", it.message) }
        } else {
            val results = days.map { day -> ApiProvider.medicalScheduleRepository.createSchedule(day, start, end, duration, doctorId) }
            val successful = results.count { it.isSuccess }
            when {
                successful == days.size -> { UiMessageManager.success(if (successful == 1) "Horario agregado" else "Horario aplicado a $successful días"); onDone(); load(doctorId) }
                successful > 0 -> { UiMessageManager.error("Se guardaron $successful de ${days.size} horarios", "Revisa los días restantes e inténtalo nuevamente."); onDone(); load(doctorId) }
                else -> UiMessageManager.error("No se pudo guardar", results.firstOrNull()?.exceptionOrNull()?.message)
            }
        }
        _state.value = _state.value.copy(saving = false)
    }

    fun delete(schedule: MedicalSchedule, onDone: () -> Unit) = viewModelScope.launch {
        _state.value = _state.value.copy(saving = true)
        ApiProvider.medicalScheduleRepository.deleteSchedule(schedule.id)
            .onSuccess { UiMessageManager.success("Horario eliminado"); onDone(); load(schedule.doctorId) }
            .onFailure { UiMessageManager.error("No se pudo eliminar", it.message) }
        _state.value = _state.value.copy(saving = false)
    }
}

@Composable
fun ScheduleManagementScreen(doctorId: Int, modifier: Modifier = Modifier) {
    val viewModel: ScheduleManagementViewModel = viewModel(key = "schedule_management_$doctorId") { ScheduleManagementViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedDay by remember(state.schedules) { mutableStateOf(state.schedules.minByOrNull { weekDays.indexOf(it.dayOfWeek) }?.dayOfWeek ?: "MONDAY") }
    var editingSchedule by remember { mutableStateOf<MedicalSchedule?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editorStart by remember { mutableStateOf("08:00") }
    var deletingSchedule by remember { mutableStateOf<MedicalSchedule?>(null) }
    LaunchedEffect(doctorId) { viewModel.load(doctorId) }
    when {
        state.loading && state.doctor == null -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null && state.doctor == null -> AppError(state.error ?: "No se pudieron cargar los horarios.") { viewModel.load(doctorId) }
        else -> Column(
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(state.doctor?.fullName ?: "Gestionar horarios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(state.doctor?.specialityName.orEmpty(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("${state.schedules.size} horarios · ${state.schedules.sumOf { it.hours() }} h semanales", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                weekDays.forEach { day ->
                    val count = state.schedules.count { it.dayOfWeek == day }
                    FilterChip(selected = selectedDay == day, onClick = { selectedDay = day }, label = { Text(if (count == 0) dayLabels.getValue(day) else "${dayLabels.getValue(day)} ($count)") }, colors = scheduleFilterChipColors())
                }
            }

            val schedules = state.schedules.filter { it.dayOfWeek == selectedDay }
            Text(dayLabels.getValue(selectedDay), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val morningSchedules = schedules.filter { it.isMorning() }
            val afternoonSchedules = schedules.filterNot { it.isMorning() }
            ShiftSection("Mañana", morningSchedules, onAdd = { editingSchedule = null; editorStart = "08:00"; showEditor = true }, onEdit = { editingSchedule = it; showEditor = true }, onDelete = { deletingSchedule = it })
            ShiftSection("Tarde", afternoonSchedules, onAdd = { editingSchedule = null; editorStart = "14:00"; showEditor = true }, onEdit = { editingSchedule = it; showEditor = true }, onDelete = { deletingSchedule = it })
        }
    }
    if (showEditor) ScheduleEditorDialog(editingSchedule, selectedDay, editorStart, state.schedules, state.saving, onDismiss = { showEditor = false }) { days, start, end, duration ->
        viewModel.save(editingSchedule, days, start, end, duration, doctorId) { showEditor = false }
    }
    AppConfirmDialog(
        visible = deletingSchedule != null,
        title = "Eliminar horario",
        message = "Se eliminará el horario del ${deletingSchedule?.dayOfWeek?.let { dayLabels[it] }}: ${deletingSchedule?.startDisplay} – ${deletingSchedule?.endDisplay}.",
        confirmText = "Eliminar",
        destructive = true,
        isLoading = state.saving,
        onConfirm = { deletingSchedule?.let { viewModel.delete(it) { deletingSchedule = null } } },
        onDismiss = { deletingSchedule = null }
    )
}

@Composable
private fun ShiftSection(title: String, schedules: List<MedicalSchedule>, onAdd: () -> Unit, onEdit: (MedicalSchedule) -> Unit, onDelete: (MedicalSchedule) -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (schedules.isEmpty()) {
                Text("Sin atención por la ${if (title == "Mañana") "mañana" else "tarde"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onAdd) { Text("Agregar horario de ${title.lowercase()}") }
            } else schedules.forEach { schedule ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${schedule.startDisplay} – ${schedule.endDisplay}", fontWeight = FontWeight.SemiBold)
                            Text("${schedule.appointmentDuration} min · ${schedule.appointmentCount()} citas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ScheduleActions(schedule, onEdit, onDelete)
                    }
                }
            }
            if (schedules.isNotEmpty()) {
                OutlinedButton(onClick = onAdd) { Text("Agregar bloque de ${title.lowercase()}") }
            }
        }
    }
}

@Composable
private fun ScheduleActions(schedule: MedicalSchedule, onEdit: (MedicalSchedule) -> Unit, onDelete: (MedicalSchedule) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, contentDescription = "Opciones del horario") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Editar") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { expanded = false; onEdit(schedule) })
            DropdownMenuItem(text = { Text("Eliminar") }, leadingIcon = { Icon(Icons.Outlined.Delete, null) }, onClick = { expanded = false; onDelete(schedule) })
        }
    }
}
