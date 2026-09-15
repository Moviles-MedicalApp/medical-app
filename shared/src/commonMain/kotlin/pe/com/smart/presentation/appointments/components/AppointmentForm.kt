package pe.com.smart.presentation.appointments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.appointments.form.AppointmentFormUiState


/*
 * =====================================================
 * FORMULARIO VISUAL DE CITAS
 * =====================================================
 *
 * Este archivo contiene los campos que el usuario
 * verá cuando quiera registrar o editar una cita.
 */
@Composable
fun AppointmentForm(

    // Estado actual de todos los campos
    state: AppointmentFormUiState,

    // Nos indica si estamos creando o editando
    isEditing: Boolean,

    // Funciones que se ejecutan cuando cambia cada campo
    onPatientIdChange: (String) -> Unit,
    onDoctorIdChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,

    // Acción del botón Guardar
    onSave: () -> Unit,

    modifier: Modifier = Modifier
) {

    /*
     * Cambiamos el título dependiendo
     * de si estamos creando o editando.
     */
    val title =
        if (isEditing) {
            "Editar cita médica"
        } else {
            "Registrar nueva cita"
        }


    /*
     * Texto que aparecerá en el botón.
     */
    val buttonText =
        if (isEditing) {
            "Guardar cambios"
        } else {
            "Registrar cita"
        }


    /*
     * El botón estará habilitado:
     *
     * - si NO está guardando
     * - y, al editar, si existen cambios
     */
    val buttonEnabled =
        !state.isSaving &&
                if (isEditing) {
                    state.hasChanges
                } else {
                    true
                }


    Column(
        modifier = modifier,

        // Espacio entre cada campo
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        /*
         * =================================================
         * TÍTULO
         * =================================================
         */
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )


        Text(
            text =
                if (isEditing) {
                    "Modifica los datos de la cita y guarda los cambios."
                } else {
                    "Completa los datos para registrar una nueva cita."
                },

            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        /*
         * =================================================
         * PACIENTE
         * =================================================
         *
         * Por ahora trabajaremos con el ID del paciente.
         * Más adelante podemos cambiarlo por una lista
         * donde aparezcan los nombres.
         */
        OutlinedTextField(
            value = state.patientId,

            onValueChange =
                onPatientIdChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("ID del paciente")
            },

            placeholder = {
                Text("Ejemplo: 1")
            },

            // Solo permite teclado numérico
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),

            // Muestra error si el paciente no es válido
            isError =
                state.patientIdError != null,

            supportingText = {

                state.patientIdError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            singleLine = true
        )


        /*
         * =================================================
         * MÉDICO
         * =================================================
         */
        OutlinedTextField(
            value = state.doctorId,

            onValueChange =
                onDoctorIdChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("ID del médico")
            },

            placeholder = {
                Text("Ejemplo: 2")
            },

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),

            isError =
                state.doctorIdError != null,

            supportingText = {

                state.doctorIdError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            singleLine = true
        )


        /*
         * =================================================
         * MOTIVO DE LA CITA
         * =================================================
         */
        OutlinedTextField(
            value = state.reason,

            onValueChange =
                onReasonChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Motivo de la cita")
            },

            placeholder = {
                Text("Ejemplo: Control general")
            },

            isError =
                state.reasonError != null,

            supportingText = {

                state.reasonError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        )


        /*
         * =================================================
         * FECHA
         * =================================================
         *
         * Por ahora escribiremos la fecha.
         *
         * Ejemplo:
         * 2026-09-15
         */
        OutlinedTextField(
            value = state.appointmentDate,

            onValueChange =
                onDateChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Fecha")
            },

            placeholder = {
                Text("2026-09-15")
            },

            isError =
                state.appointmentDateError != null,

            supportingText = {

                state.appointmentDateError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            singleLine = true
        )


        /*
         * =================================================
         * HORA DE INICIO
         * =================================================
         */
        OutlinedTextField(
            value = state.startTime,

            onValueChange =
                onStartTimeChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Hora de inicio")
            },

            placeholder = {
                Text("09:00")
            },

            isError =
                state.startTimeError != null,

            supportingText = {

                state.startTimeError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            singleLine = true
        )


        /*
         * =================================================
         * HORA DE FIN
         * =================================================
         */
        OutlinedTextField(
            value = state.endTime,

            onValueChange =
                onEndTimeChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Hora de fin")
            },

            placeholder = {
                Text("09:30")
            },

            isError =
                state.endTimeError != null,

            supportingText = {

                state.endTimeError?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            singleLine = true
        )


        /*
         * =================================================
         * ESTADO
         * =================================================
         *
         * Ejemplos:
         *
         * PROGRAMADA
         * ATENDIDA
         * CANCELADA
         */
        OutlinedTextField(
            value = state.status,

            onValueChange =
                onStatusChange,

            modifier =
                Modifier.fillMaxWidth(),

            label = {
                Text("Estado")
            },

            placeholder = {
                Text("PROGRAMADA")
            },

            singleLine = true
        )


        /*
         * =================================================
         * BOTÓN GUARDAR
         * =================================================
         */
        Button(
            onClick = onSave,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                buttonEnabled
        ) {

            /*
             * Mientras se está guardando,
             * mostramos un indicador de carga.
             */
            if (state.isSaving) {

                CircularProgressIndicator()

            } else {

                // Icono de guardar
                Icon(
                    imageVector =
                        Icons.Outlined.Save,

                    contentDescription =
                        "Guardar cita",

                    modifier =
                        Modifier.padding(
                            end = 8.dp
                        )
                )


                // Texto del botón
                Text(
                    text = buttonText
                )
            }
        }
    }
}