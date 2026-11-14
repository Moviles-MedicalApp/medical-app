package pe.com.smart.presentation.specialities.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

/**
 * Formulario de registro y edición de especialidades.
 *
 * Conserva las validaciones y los callbacks
 * del SpecialityFormViewModel.
 */
@Composable
fun SpecialityForm(
    state: SpecialityFormUiState,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeletedChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    val canSave = !state.isLoading &&
            !state.isSaving &&
            (!isEditing || state.hasChanges)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ENCABEZADO
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isEditing) {
                    "Editar especialidad"
                } else {
                    "Nueva especialidad"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Text(
                text = if (isEditing) {
                    "Actualiza los datos y la disponibilidad de esta especialidad."
                } else {
                    "Registra una especialidad para organizar las atenciones médicas."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        // INFORMACIÓN GENERAL
        SpecialityFormSection(
            title = "Información general",
            subtitle = "Nombre y descripción de la especialidad"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Text(
                    text = "Datos de la especialidad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre de la especialidad")
                },
                placeholder = {
                    Text("Ej. Cardiología")
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.MedicalServices,
                        contentDescription = null
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Words
                ),
                isError = state.nameError != null,
                supportingText = state.nameError?.let { error ->
                    {
                        Text(error)
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Descripción")
                },
                placeholder = {
                    Text(
                        "Describe brevemente las consultas de esta especialidad"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Description,
                        contentDescription = null
                    )
                },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization =
                        KeyboardCapitalization.Sentences
                ),
                isError = state.descriptionError != null,
                supportingText = {
                    Text(
                        text = state.descriptionError
                            ?: "Describe el área de atención médica."
                    )
                },
                shape = RoundedCornerShape(14.dp)
            )
        }

        // DISPONIBILIDAD
        if (isEditing) {
            SpecialityFormSection(
                title = "Disponibilidad",
                subtitle = "Control del estado de la especialidad"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Especialidad activa",
                            style = MaterialTheme
                                .typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (!state.deleted) {
                                "Disponible para las consultas"
                            } else {
                                "Actualmente deshabilitada"
                            },
                            style = MaterialTheme
                                .typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = !state.deleted,
                        onCheckedChange = { active ->
                            onDeletedChange(!active)
                        },
                        enabled = !state.isSaving
                    )
                }

                if (state.deleted) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colors.onErrorContainer,
                                modifier = Modifier.size(19.dp)
                            )

                            Text(
                                text = "La especialidad quedará marcada como inactiva.",
                                style = MaterialTheme
                                    .typography.bodySmall,
                                color = colors.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // NOTA
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = colors.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.primary
                )

                Text(
                    text = "Verifica la información antes de guardar. El nombre es obligatorio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer
                )
            }
        }

        // GUARDAR
        Button(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp,
                    color = colors.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = if (state.isSaving) {
                    "Guardando..."
                } else {
                    buttonText
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (
            isEditing &&
            !state.hasChanges &&
            !state.isSaving
        ) {
            Text(
                text = "No hay modificaciones pendientes.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpecialityFormSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )

            content()
        }
    }
}
