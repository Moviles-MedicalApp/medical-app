package pe.com.smart.presentation.specialities.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.specialities.form.SpecialityFormUiState

@Composable
fun SpecialityForm(
    state: SpecialityFormUiState,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    buttonText: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /*
         * Nombre
         */
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de la especialidad") },
            placeholder = { Text("Ej: Cardiología") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null
                )
            },
            isError = state.nameError != null,
            supportingText = state.nameError?.let {
                { Text(text = it) }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        /*
         * Descripción
         */
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descripción") },
            placeholder = { Text("Breve descripción del área de atención...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null
                )
            },
            isError = state.descriptionError != null,
            supportingText = state.descriptionError?.let {
                { Text(text = it) }
            },
            minLines = 3,
            maxLines = 5,
            shape = MaterialTheme.shapes.medium
        )

        /*
         * Estado (Activa / Inactiva) - Solo al editar
         */
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Especialidad activa",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (state.active) "Disponible para consultas" else "Deshabilitada temporalmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = state.active,
                    onCheckedChange = onActiveChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*
         * Botón Guardar
         */
        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (state.isSaving) "Guardando..." else buttonText,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
