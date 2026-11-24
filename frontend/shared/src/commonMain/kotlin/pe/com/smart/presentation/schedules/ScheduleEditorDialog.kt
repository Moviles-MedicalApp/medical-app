
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.domain.model.MedicalSchedule

private val editorDays = listOf(
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY",
    "SATURDAY",
    "SUNDAY"
)

private val durationPresets = listOf(
    15, 20, 30, 45, 60
)

/**
 * Editor de horarios médicos.
 *
 * Conserva la firma utilizada por
 * ScheduleManagementScreen.
 *
 * Horarios enviados al repositorio: HH:mm:ss.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleEditorDialog(
    schedule: MedicalSchedule?,
    initialDay: String,
    initialStart: String,
    schedules: List<MedicalSchedule>,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Set<String>, String, String, Int) -> Unit
) {
    val isEditing = schedule != null
    val colors = MaterialTheme.colorScheme

    var days by remember(schedule?.id, initialDay) {
        mutableStateOf(
            setOf(schedule?.dayOfWeek ?: initialDay)
        )
    }

    var start by remember(schedule?.id, initialStart) {
        mutableStateOf(
            editorDisplayTime(
                schedule?.startDisplay ?: initialStart
            )
        )
    }

    var end by remember(schedule?.id, initialStart) {
        mutableStateOf(
            editorDisplayTime(
                schedule?.endDisplay
                    ?: if (initialStart == "14:00") {
                        "18:00"
                    } else {
                        "12:00"
                    }
            )
        )
    }

    var duration by remember(schedule?.id) {
        mutableStateOf(
            schedule?.appointmentDuration
                ?.toString() ?: "30"
        )
    }

    var validationError by remember(
        schedule?.id,
        initialDay
    ) {
        mutableStateOf<String?>(null)
    }

    var showMultiDayConfirmation by remember {
        mutableStateOf(false)
    }

    var editingTime by remember {
        mutableStateOf<EditorTimeTarget?>(null)
    }

    val capacity = editorAppointmentCount(
        start = start,
        end = end,
        duration = duration
    )

    val validDays = days.filter {
        it in editorDays
    }.toSet()

    val selectedDaysText = editorDays
        .filter { it in validDays }
        .joinToString(", ") {
            dayLabels[it] ?: it
        }

    fun submitValidated() {
        val startValue = editorNormalizeTime(start)
        val endValue = editorNormalizeTime(end)
        val minutes = duration.toIntOrNull()

        val overlaps =
            startValue != null &&
                    endValue != null &&
                    schedules.any { existing ->
                        existing.id != schedule?.id &&
                                existing.dayOfWeek in validDays &&
                                startValue < existing.endTime &&
                                endValue > existing.startTime
                    }

        validationError = when {
            validDays.isEmpty() ->
                "Selecciona al menos un día."

            startValue == null || endValue == null ->
                "Selecciona horas válidas."

            startValue >= endValue ->
                "La hora de inicio debe ser anterior a la hora de fin."

            minutes == null || minutes <= 0 ->
                "Ingresa una duración válida."

            (
                    editorToMinutes(endValue) -
                            editorToMinutes(startValue)
                    ) % minutes != 0 ->
                "La duración debe dividir exactamente el rango horario."

            overlaps ->
                "El horario se cruza con otro bloque del mismo día."

            else -> null
        }

        if (validationError == null) {
            if (!isEditing && validDays.size > 1) {
                showMultiDayConfirmation = true
            } else {
                onSave(
                    validDays,
                    startValue!!,
                    endValue!!,
                    minutes!!
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!saving) onDismiss()
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (isEditing) {
                        "Editar horario"
                    } else {
                        "Nuevo horario médico"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isEditing) {
                        "Actualiza el rango de atención y la duración de las citas."
                    } else {
                        "Configura los días y las horas en que atenderá el médico."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            EditorSection(
                title = "Días de atención",
                subtitle = if (isEditing) {
                    "El día del horario no se modifica desde este editor."
                } else {
                    "Puedes aplicar un mismo horario a varios días."
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    editorDays.forEach { day ->
                        FilterChip(
                            selected = day in validDays,
                            enabled = !saving && !isEditing,
                            onClick = {
                                days = if (day in days) {
                                    days - day
                                } else {
                                    days + day
                                }

                                validationError = null
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

                if (!isEditing && validDays.size > 1) {
                    EditorInfo(
                        message = "El horario se aplicará a: $selectedDaysText"
                    )
                }
            }

            EditorSection(
                title = "Horario de atención",
                subtitle = "Selecciona el inicio y el final del turno."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    EditorTimeField(
                        label = "Inicio",
                        time = start,
                        enabled = !saving,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editingTime = EditorTimeTarget.START
                        }
                    )

                    EditorTimeField(
                        label = "Fin",
                        time = end,
                        enabled = !saving,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            editingTime = EditorTimeTarget.END
                        }
                    )
                }
            }

            EditorSection(
                title = "Duración por cita",
                subtitle = "Tiempo asignado a cada atención."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    durationPresets.forEach { minutes ->
                        FilterChip(
                            selected =
                                duration == minutes.toString(),
                            enabled = !saving,
                            onClick = {
                                duration = minutes.toString()
                                validationError = null
                            },
                            label = {
                                Text("$minutes min")
                            },
                            colors = scheduleFilterChipColors()
                        )
                    }
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { value ->
                        duration = value
                            .filter { it.isDigit() }
                            .take(4)

                        validationError = null
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Minutos por cita")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Vista previa de capacidad.
            EditorCapacityCard(
                capacityPerDay = capacity,
                selectedDays = validDays.size,
                start = start,
                end = end,
                duration = duration
            )

            if (!validationError.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.errorContainer
                ) {
                    Text(
                        text = validationError.orEmpty(),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    submitValidated()
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(19.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    if (saving) {
                        "Guardando..."
                    } else {
                        "Guardar horario"
                    }
                )
            }

            OutlinedButton(
                onClick = onDismiss,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar")
            }

            Spacer(Modifier.size(12.dp))
        }
    }

    // Selector visual para horas.
    editingTime?.let { target ->
        EditorTimePicker(
            title = if (target == EditorTimeTarget.START) {
                "Hora de inicio"
            } else {
                "Hora de fin"
            },
            currentTime = if (
                target == EditorTimeTarget.START
            ) {
                start
            } else {
                end
            },
            onDismiss = {
                editingTime = null
            },
            onConfirm = { selected ->
                if (target == EditorTimeTarget.START) {
                    start = selected
                } else {
                    end = selected
                }

                validationError = null
                editingTime = null
            }
        )
    }

    AppConfirmDialog(
        visible = showMultiDayConfirmation,
        title = "Aplicar horario a varios días",
        message = "Se creará el horario $start–$end, " +
                "con citas de $duration minutos, para: " +
                selectedDaysText + ".",
        confirmText = "Aplicar horario",
        cancelText = "Volver",
        isLoading = saving,
        onConfirm = {
            if (!saving) {
                val startValue = editorNormalizeTime(start)
                    ?: return@AppConfirmDialog

                val endValue = editorNormalizeTime(end)
                    ?: return@AppConfirmDialog

                val minutes = duration.toIntOrNull()
                    ?: return@AppConfirmDialog

                // Se cierra la confirmación y se
                // entrega la solicitud al ViewModel.
                showMultiDayConfirmation = false

                onSave(
                    validDays,
                    startValue,
                    endValue,
                    minutes
                )
            }
        },
        onDismiss = {
            if (!saving) {
                showMultiDayConfirmation = false
            }
        }
    )
}

private enum class EditorTimeTarget {
    START,
    END
}

@Composable
private fun EditorSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            HorizontalDivider(
                color = colors.outlineVariant
            )

            content()
        }
    }
}

@Composable
private fun EditorInfo(
    message: String
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.primaryContainer,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.primary
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onPrimaryContainer
            )
        }
    }
}

/**
 * Botón de selección de hora.
 * Evita que el usuario tenga que escribir
 * manualmente el formato HH:mm.
 */
@Composable
private fun EditorTimeField(
    label: String,
    time: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(7.dp))

            Text(time)
        }
    }
}

/**
 * Selector de hora y minutos en formato 24 h.
 * Se permiten todos los minutos 00..59,
 * conservando horarios existentes no estándar.
 */
@Composable
private fun EditorTimePicker(
    title: String,
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = currentTime.split(":")

    var hour by remember(currentTime) {
        mutableStateOf(
            parts.getOrNull(0)?.toIntOrNull() ?: 8
        )
    }

    var minute by remember(currentTime) {
        mutableStateOf(
            parts.getOrNull(1)?.toIntOrNull() ?: 0
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Selecciona la hora y los minutos.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorNumberSelector(
                        label = "Hora",
                        value = hour,
                        range = 0..23,
                        onSelected = { hour = it },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme
                            .typography.headlineMedium
                    )

                    EditorNumberSelector(
                        label = "Minutos",
                        value = minute,
                        range = 0..59,
                        onSelected = { minute = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        editorPad(hour) + ":" +
                                editorPad(minute)
                    )
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EditorNumberSelector(
    label: String,
    value: Int,
    range: IntRange,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Box {
            OutlinedButton(
                onClick = {
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = editorPad(value),
                    fontWeight = FontWeight.Bold
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.heightIn(
                    max = 260.dp
                )
            ) {
                range.forEach { number ->
                    DropdownMenuItem(
                        text = {
                            Text(editorPad(number))
                        },
                        onClick = {
                            onSelected(number)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorCapacityCard(
    capacityPerDay: Int?,
    selectedDays: Int,
    start: String,
    end: String,
    duration: String
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = colors.primary
                )

                Text(
                    text = "Capacidad del horario",
                    style = MaterialTheme
                        .typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onPrimaryContainer
                )
            }

            if (capacityPerDay != null) {
                Text(
                    text = "$capacityPerDay citas por día",
                    style = MaterialTheme
                        .typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )

                if (selectedDays > 1) {
                    Text(
                        text = "${capacityPerDay * selectedDays} " +
                                "cupos teóricos por semana",
                        style = MaterialTheme
                            .typography.bodyMedium,
                        color = colors.onPrimaryContainer
                    )
                }

                Text(
                    text = "$start–$end · $duration min por cita",
                    style = MaterialTheme
                        .typography.bodySmall,
                    color = colors.onPrimaryContainer
                )
            } else {
                Text(
                    text = "Selecciona un rango y una duración compatibles para calcular los cupos.",
                    style = MaterialTheme
                        .typography.bodySmall,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Helpers exclusivos del editor.
 */
private fun editorAppointmentCount(
    start: String,
    end: String,
    duration: String
): Int? {
    val startValue = editorNormalizeTime(start)
        ?: return null

    val endValue = editorNormalizeTime(end)
        ?: return null

    val minutes = duration.toIntOrNull()
        ?.takeIf { it > 0 }
        ?: return null

    val range = editorToMinutes(endValue) -
            editorToMinutes(startValue)

    if (range <= 0 || range % minutes != 0) {
        return null
    }

    return range / minutes
}

private fun editorNormalizeTime(
    value: String
): String? {
    val match = Regex(
        "^([01]\\d|2[0-3]):[0-5]\\d(?::[0-5]\\d)?$"
    ).matchEntire(value.trim()) ?: return null

    return match.value.take(5) + ":00"
}

private fun editorDisplayTime(
    value: String
): String {
    return editorNormalizeTime(value)
        ?.take(5)
        ?: value.take(5)
}

private fun editorToMinutes(
    value: String
): Int {
    return value.substring(0, 2).toInt() * 60 +
            value.substring(3, 5).toInt()
}

private fun editorPad(
    value: Int
): String = value.toString().padStart(2, '0')
