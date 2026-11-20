
package pe.com.smart.presentation.schedules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.message.UiMessageManager
import org.koin.compose.koinInject
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.domain.usecase.MedicalScheduleUseCase
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule

private data class ManagementState(
    val doctor: Doctor? = null,
    val schedules: List<MedicalSchedule> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

/**
 * Conserva los métodos utilizados
 * por la pantalla y ScheduleEditorDialog.
 */
private class ScheduleManagementViewModel(
    private val doctorUseCase: DoctorUseCase,
    private val medicalScheduleUseCase: MedicalScheduleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ManagementState()
    )

    val state = _state.asStateFlow()

    fun load(doctorId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null
            )

            try {
                val doctorResult =
                    doctorUseCase
                        .getDoctorById(doctorId)

                val schedulesResult =
                    medicalScheduleUseCase
                        .getSchedulesByDoctor(doctorId)

                _state.value = _state.value.copy(
                    doctor = doctorResult.getOrNull(),
                    schedules =
                        schedulesResult.getOrDefault(emptyList()),
                    loading = false,
                    error =
                        doctorResult.exceptionOrNull()?.message
                            ?: schedulesResult
                                .exceptionOrNull()?.message
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = exception.message
                        ?: "No se pudieron cargar los horarios."
                )
            }
        }
    }

    fun save(
        schedule: MedicalSchedule?,
        days: Set<String>,
        start: String,
        end: String,
        duration: Int,
        doctorId: Int,
        onDone: () -> Unit
    ) {
        if (_state.value.saving || days.isEmpty()) {
            return
        }

        _state.value = _state.value.copy(
            saving = true
        )

        viewModelScope.launch {
            try {
                if (schedule != null) {
                    medicalScheduleUseCase
                        .updateSchedule(
                            schedule.id,
                            days.first(),
                            start,
                            end,
                            duration,
                            doctorId
                        )
                        .getOrThrow()

                    UiMessageManager.success(
                        "Horario actualizado"
                    )

                    onDone()
                    load(doctorId)
                } else {
                    val results = days.map { day ->
                        medicalScheduleUseCase
                            .createSchedule(
                                day,
                                start,
                                end,
                                duration,
                                doctorId
                            )
                    }

                    val successful = results.count {
                        it.isSuccess
                    }

                    when {
                        successful == days.size -> {
                            UiMessageManager.success(
                                if (successful == 1) {
                                    "Horario agregado"
                                } else {
                                    "Horario aplicado a $successful días"
                                }
                            )

                            onDone()
                            load(doctorId)
                        }

                        successful > 0 -> {
                            UiMessageManager.error(
                                "Se guardaron $successful de ${days.size} horarios",
                                "Revisa los días restantes antes de intentarlo nuevamente."
                            )

                            onDone()
                            load(doctorId)
                        }

                        else -> {
                            UiMessageManager.error(
                                "No se pudo guardar",
                                results.firstOrNull()
                                    ?.exceptionOrNull()
                                    ?.message
                            )
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                UiMessageManager.error(
                    "No se pudo guardar",
                    exception.message
                )
            } finally {
                _state.value = _state.value.copy(
                    saving = false
                )
            }
        }
    }

    fun delete(
        schedule: MedicalSchedule,
        onDone: () -> Unit
    ) {
        if (_state.value.saving) {
            return
        }

        _state.value = _state.value.copy(
            saving = true
        )

        viewModelScope.launch {
            try {
                medicalScheduleUseCase
                    .deleteSchedule(schedule.id)
                    .getOrThrow()

                UiMessageManager.success(
                    "Horario eliminado"
                )

                onDone()
                load(schedule.doctorId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                UiMessageManager.error(
                    "No se pudo eliminar",
                    exception.message
                )
            } finally {
                _state.value = _state.value.copy(
                    saving = false
                )
            }
        }
    }
}

@Composable
fun ScheduleManagementScreen(
    doctorId: Int,
    modifier: Modifier = Modifier
) {
    val doctorUseCase: DoctorUseCase = koinInject()
    val medicalScheduleUseCase: MedicalScheduleUseCase = koinInject()
    val viewModel: ScheduleManagementViewModel = viewModel(
        key = "schedule_management_$doctorId"
    ) {
        ScheduleManagementViewModel(doctorUseCase, medicalScheduleUseCase)
    }

    val state by viewModel.state
        .collectAsStateWithLifecycle()

    // No reiniciar el día al actualizar los horarios.
    var selectedDay by remember(doctorId) {
        mutableStateOf("MONDAY")
    }

    var editingSchedule by remember(doctorId) {
        mutableStateOf<MedicalSchedule?>(null)
    }

    var showEditor by remember(doctorId) {
        mutableStateOf(false)
    }

    var editorStart by remember(doctorId) {
        mutableStateOf("08:00")
    }

    var deletingSchedule by remember(doctorId) {
        mutableStateOf<MedicalSchedule?>(null)
    }

    LaunchedEffect(doctorId, viewModel) {
        viewModel.load(doctorId)
    }

    val selectedSchedules = state.schedules
        .filter { it.dayOfWeek == selectedDay }
        .sortedBy { it.startTime }

    val morningSchedules = selectedSchedules.filter {
        it.isMorning()
    }

    val afternoonSchedules = selectedSchedules.filterNot {
        it.isMorning()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        when {
            state.loading && state.doctor == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.doctor == null -> {
                AppError(
                    message = state.error
                        ?: "No se pudieron cargar los horarios.",
                    onRetry = {
                        viewModel.load(doctorId)
                    }
                )
            }

            else -> {
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
                    ScheduleManagementHeader(
                        doctor = state.doctor,
                        schedules = state.schedules
                    )

                    if (state.loading) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
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

                    if (!state.error.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme
                                .colorScheme.errorContainer
                        ) {
                            Text(
                                text = state.error.orEmpty(),
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme
                                    .colorScheme.onErrorContainer
                            )
                        }
                    }

                    Text(
                        text = "Seleccionar día",
                        style = MaterialTheme
                            .typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        val orderedDays = listOf(
                            "MONDAY",
                            "TUESDAY",
                            "WEDNESDAY",
                            "THURSDAY",
                            "FRIDAY",
                            "SATURDAY",
                            "SUNDAY"
                        )

                        orderedDays.forEach { day ->
                            val count = state.schedules.count {
                                it.dayOfWeek == day
                            }

                            FilterChip(
                                selected = selectedDay == day,
                                onClick = {
                                    selectedDay = day
                                },
                                label = {
                                    Text(
                                        if (count > 0) {
                                            "${dayLabels[day]} ($count)"
                                        } else {
                                            dayLabels[day] ?: day
                                        }
                                    )
                                },
                                colors = scheduleFilterChipColors()
                            )
                        }
                    }

                    Text(
                        text = dayLabels[selectedDay]
                            ?: selectedDay,
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ShiftSection(
                        title = "Mañana",
                        schedules = morningSchedules,
                        isMorning = true,
                        enabled = !state.saving && !state.loading,
                        onAdd = {
                            editingSchedule = null
                            editorStart = "08:00"
                            showEditor = true
                        },
                        onEdit = {
                            editingSchedule = it
                            showEditor = true
                        },
                        onDelete = {
                            deletingSchedule = it
                        }
                    )

                    ShiftSection(
                        title = "Tarde",
                        schedules = afternoonSchedules,
                        isMorning = false,
                        enabled = !state.saving && !state.loading,
                        onAdd = {
                            editingSchedule = null
                            editorStart = "14:00"
                            showEditor = true
                        },
                        onEdit = {
                            editingSchedule = it
                            showEditor = true
                        },
                        onDelete = {
                            deletingSchedule = it
                        }
                    )
                }
            }
        }
    }

    if (showEditor) {
        ScheduleEditorDialog(
            schedule = editingSchedule,
            initialDay = selectedDay,
            initialStart = editorStart,
            schedules = state.schedules,
            saving = state.saving,
            onDismiss = {
                if (!state.saving) {
                    showEditor = false
                }
            },
            onSave = { days, start, end, duration ->
                viewModel.save(
                    schedule = editingSchedule,
                    days = days,
                    start = start,
                    end = end,
                    duration = duration,
                    doctorId = doctorId,
                    onDone = {
                        showEditor = false
                    }
                )
            }
        )
    }

    AppConfirmDialog(
        visible = deletingSchedule != null,
        title = "Eliminar horario",
        message = deletingSchedule?.let {
            "Se eliminará el horario del " +
                    "${dayLabels[it.dayOfWeek] ?: it.dayOfWeek}, " +
                    "de ${it.startDisplay} a ${it.endDisplay}."
        } ?: "",
        confirmText = "Eliminar",
        cancelText = "Cancelar",
        destructive = true,
        isLoading = state.saving,
        onConfirm = {
            deletingSchedule?.let { schedule ->
                if (!state.saving) {
                    viewModel.delete(schedule) {
                        deletingSchedule = null
                    }
                }
            }
        },
        onDismiss = {
            if (!state.saving) {
                deletingSchedule = null
            }
        }
    )
}

@Composable
private fun ScheduleManagementHeader(
    doctor: Doctor?,
    schedules: List<MedicalSchedule>
) {
    val colors = MaterialTheme.colorScheme

    val totalMinutes = schedules.sumOf { schedule ->
        val startParts = schedule.startTime.split(":")
        val endParts = schedule.endTime.split(":")

        val start = (
                (startParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60
                ) + (startParts.getOrNull(1)?.toIntOrNull() ?: 0)

        val end = (
                (endParts.getOrNull(0)?.toIntOrNull() ?: 0) * 60
                ) + (endParts.getOrNull(1)?.toIntOrNull() ?: 0)

        (end - start).coerceAtLeast(0)
    }

    val hoursText = if (totalMinutes % 60 == 0) {
        "${totalMinutes / 60} h"
    } else {
        "${totalMinutes / 60} h ${totalMinutes % 60} min"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = doctor?.fullName
                            ?: "Gestión de horarios",
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = doctor?.specialityName.orEmpty(),
                        style = MaterialTheme
                            .typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "${schedules.size} bloques",
                    style = MaterialTheme
                        .typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$hoursText semanales",
                    style = MaterialTheme
                        .typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShiftSection(
    title: String,
    schedules: List<MedicalSchedule>,
    isMorning: Boolean,
    enabled: Boolean,
    onAdd: () -> Unit,
    onEdit: (MedicalSchedule) -> Unit,
    onDelete: (MedicalSchedule) -> Unit
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(13.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    imageVector = if (isMorning) {
                        Icons.Outlined.WbSunny
                    } else {
                        Icons.Outlined.NightsStay
                    },
                    contentDescription = null,
                    tint = colors.primary
                )

                Text(
                    text = title,
                    style = MaterialTheme
                        .typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            if (schedules.isEmpty()) {
                Text(
                    text = "Sin bloques de atención configurados.",
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                schedules.forEach { schedule ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text =
                                        "${schedule.startDisplay} – ${schedule.endDisplay}",
                                    style = MaterialTheme
                                        .typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text =
                                        "${schedule.appointmentDuration} min por cita · " +
                                                "${schedule.appointmentCount()} citas",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }

                            ScheduleActions(
                                schedule = schedule,
                                enabled = enabled,
                                onEdit = onEdit,
                                onDelete = onDelete
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onAdd,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Agregar bloque de ${title.lowercase()}"
                )
            }
        }
    }
}

@Composable
private fun ScheduleActions(
    schedule: MedicalSchedule,
    enabled: Boolean,
    onEdit: (MedicalSchedule) -> Unit,
    onDelete: (MedicalSchedule) -> Unit
) {
    var expanded by remember(schedule.id) {
        mutableStateOf(false)
    }

    Box {
        IconButton(
            onClick = {
                expanded = true
            },
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "Opciones del horario"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onEdit(schedule)
                }
            )

            DropdownMenuItem(
                text = { Text("Eliminar") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onDelete(schedule)
                }
            )
        }
    }
}
