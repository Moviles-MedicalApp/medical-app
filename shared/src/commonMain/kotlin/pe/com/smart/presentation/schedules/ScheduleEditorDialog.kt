package pe.com.smart.presentation.schedules

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.core.ui.AppConfirmDialog

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ScheduleEditorDialog(
    schedule: MedicalSchedule?, initialDay: String, initialStart: String,
    schedules: List<MedicalSchedule>, saving: Boolean, onDismiss: () -> Unit,
    onSave: (Set<String>, String, String, Int) -> Unit
) {
    var days by remember(schedule, initialDay) { mutableStateOf(setOf(schedule?.dayOfWeek ?: initialDay)) }
    var start by remember(schedule, initialStart) { mutableStateOf(schedule?.startDisplay ?: initialStart) }
    var end by remember(schedule, initialStart) { mutableStateOf(schedule?.endDisplay ?: if (initialStart == "14:00") "18:00" else "12:00") }
    var duration by remember(schedule) { mutableStateOf(schedule?.appointmentDuration?.toString() ?: "30") }
    var error by remember { mutableStateOf<String?>(null) }
    var showMultiDayConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = { if (!saving) onDismiss() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (schedule == null) "Nuevo horario" else "Editar horario", style = MaterialTheme.typography.headlineSmall)
            Text(if (schedule == null) "Selecciona los días, el rango y la duración de cada cita." else "Actualiza el rango y la duración de las citas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Días", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                weekDays.forEach { value ->
                    FilterChip(value in days, { if (schedule == null) days = if (value in days) days - value else days + value }, enabled = schedule == null, label = { Text(dayLabels.getValue(value)) })
                }
            }
            if (schedule == null && days.size > 1) Text("Se aplicará el mismo horario a ${weekDays.filter { it in days }.joinToString { dayLabels.getValue(it) }}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("Rango y duración", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(start, { start = it }, Modifier.fillMaxWidth(), label = { Text("Hora de inicio") }, placeholder = { Text("08:00") }, singleLine = true)
            OutlinedTextField(end, { end = it }, Modifier.fillMaxWidth(), label = { Text("Hora de fin") }, placeholder = { Text("12:00") }, singleLine = true)
            OutlinedTextField(duration, { duration = it }, Modifier.fillMaxWidth(), label = { Text("Minutos por cita") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            appointmentCount(start, end, duration)?.let { Text("$it citas disponibles", color = MaterialTheme.colorScheme.primary) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                val startValue = normalizeTime(start)
                val endValue = normalizeTime(end)
                val minutes = duration.toIntOrNull()
                val overlaps = startValue != null && endValue != null && schedules.any { it.id != schedule?.id && it.dayOfWeek in days && startValue < it.endTime && endValue > it.startTime }
                error = when {
                    days.isEmpty() -> "Selecciona al menos un día."
                    startValue == null || endValue == null -> "Usa el formato de hora HH:mm."
                    startValue >= endValue -> "La hora de inicio debe ser menor que la final."
                    minutes == null || minutes <= 0 -> "Ingresa una duración válida."
                    (toMinutes(endValue) - toMinutes(startValue)) % minutes != 0 -> "La duración debe dividir el rango exacto."
                    overlaps -> "Este horario se cruza con otro bloque del mismo día."
                    else -> null
                }
                if (error == null) {
                    if (schedule == null && days.size > 1) showMultiDayConfirmation = true
                    else onSave(days, startValue!!, endValue!!, minutes!!)
                }
            }, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Guardando..." else "Guardar horario") }
            OutlinedButton(onClick = onDismiss, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
    AppConfirmDialog(
        visible = showMultiDayConfirmation,
        title = "Aplicar horario a varios días",
        message = "Se creará el horario ${start}–${end} para ${weekDays.filter { it in days }.joinToString { dayLabels.getValue(it) }}.",
        confirmText = "Aplicar horario",
        onConfirm = {
            val startValue = normalizeTime(start) ?: return@AppConfirmDialog
            val endValue = normalizeTime(end) ?: return@AppConfirmDialog
            val minutes = duration.toIntOrNull() ?: return@AppConfirmDialog
            showMultiDayConfirmation = false
            onSave(days, startValue, endValue, minutes)
        },
        onDismiss = { showMultiDayConfirmation = false }
    )
}

private fun appointmentCount(start: String, end: String, duration: String): Int? {
    val startValue = normalizeTime(start) ?: return null
    val endValue = normalizeTime(end) ?: return null
    val minutes = duration.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val range = toMinutes(endValue) - toMinutes(startValue)
    return (range / minutes).takeIf { range > 0 && range % minutes == 0 }
}

private fun normalizeTime(value: String): String? {
    val match = Regex("^([01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?$").matchEntire(value.trim()) ?: return null
    return match.value.take(5) + ":00"
}

private fun toMinutes(value: String): Int = value.substring(0, 2).toInt() * 60 + value.substring(3, 5).toInt()
