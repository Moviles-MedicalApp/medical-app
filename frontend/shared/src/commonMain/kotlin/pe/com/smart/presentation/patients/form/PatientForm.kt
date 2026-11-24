
package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.patients.form.PatientFormUiState

/**
 * Formulario reutilizable para registrar y editar pacientes.
 *
 * La validación y el estado pertenecen al ViewModel.
 * Este componente se limita a presentarlos.
 */
@Composable
fun PatientForm(
    state: PatientFormUiState,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDniChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    val canSave = !state.isSaving &&
            (!isEditing || state.hasChanges)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // =================================================
        // HEADER
        // =================================================

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isEditing) {
                    "Editar información"
                } else {
                    "Registrar paciente"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Text(
                text = if (isEditing) {
                    "Actualiza los datos del paciente y guarda las modificaciones."
                } else {
                    "Completa los datos personales y de contacto del nuevo paciente."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        // =================================================
        // IDENTITY SECTION
        // =================================================

        PatientFormSection(
            title = "Datos personales",
            subtitle = "Información de identificación del paciente",
            icon = Icons.Outlined.Badge
        ) {

            PatientFormField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Nombres",
                placeholder = "Ingresa los nombres",
                icon = Icons.Outlined.Person,
                error = state.nameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            PatientFormField(
                value = state.lastName,
                onValueChange = onLastNameChange,
                label = "Apellidos",
                placeholder = "Ingresa los apellidos",
                icon = Icons.Outlined.Person,
                error = state.lastNameError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            PatientFormField(
                value = state.dni,
                onValueChange = onDniChange,
                label = "DNI",
                placeholder = "8 dígitos",
                icon = Icons.Outlined.ContactPage,
                error = state.dniError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                helper = "${state.dni.length}/8 dígitos"
            )
        }

        // =================================================
        // CONTACT SECTION
        // =================================================

        PatientFormSection(
            title = "Datos de contacto",
            subtitle = "Información para comunicarse con el paciente",
            icon = Icons.Outlined.Phone
        ) {

            PatientFormField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Correo electrónico",
                placeholder = "ejemplo@correo.com",
                icon = Icons.Outlined.Email,
                error = state.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            PatientFormField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = "Número de celular",
                placeholder = "9 dígitos",
                icon = Icons.Outlined.Phone,
                error = state.phoneError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                helper = "${state.phone.length}/9 dígitos"
            )
        }

        // =================================================
        // INFORMATION
        // =================================================

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = colors.primaryContainer.copy(alpha = 0.55f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Todos los campos son obligatorios. Verifica la información antes de guardar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer
                )
            }
        }

        // =================================================
        // SAVE BUTTON
        // =================================================

        Button(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.width(10.dp))

            Text(
                text = if (state.isSaving) {
                    "Guardando..."
                } else {
                    buttonText
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }

        if (isEditing && !state.hasChanges && !state.isSaving) {
            Text(
                text = "No hay modificaciones pendientes.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

/**
 * Tarjeta de una sección del formulario.
 */
@Composable
private fun PatientFormSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
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
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            content()
        }
    }
}

/**
 * Campo reutilizable con validación visual.
 */
@Composable
private fun PatientFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
    helper: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            isError = error != null,
            supportingText = if (error != null || helper != null) {
                {
                    Text(
                        text = error ?: helper.orEmpty(),
                        color = if (error != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            } else {
                null
            },
            shape = RoundedCornerShape(14.dp)
        )
    }
}
