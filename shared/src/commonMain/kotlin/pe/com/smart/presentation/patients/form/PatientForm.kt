package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.patients.form.PatientFormUiState

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

    val title =
        if (isEditing) {
            "Actualizar datos del paciente"
        } else {
            "Registrar nuevo paciente"
        }

    val description =
        if (isEditing) {
            "Modifica la información del paciente y guarda los cambios."
        } else {
            "Completa la información requerida para registrar un nuevo paciente."
        }

    val buttonEnabled =
        !state.isSaving &&
                if (isEditing) {
                    state.hasChanges
                } else {
                    true
                }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /*
         * =============================================
         * CABECERA DEL FORMULARIO
         * =============================================
         */
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        /*
         * =============================================
         * TARJETA DE INFORMACIÓN
         * =============================================
         */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Datos del paciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Los campos son obligatorios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Nombre")
                    },
                    placeholder = {
                        Text("Ingrese el nombre")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = {
                        state.nameError?.let {
                            Text(it)
                        }
                    }
                )

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = onLastNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Apellido")
                    },
                    placeholder = {
                        Text("Ingrese el apellido")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = state.lastNameError != null,
                    supportingText = {
                        state.lastNameError?.let {
                            Text(it)
                        }
                    }
                )

                OutlinedTextField(
                    value = state.dni,
                    onValueChange = onDniChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("DNI")
                    },
                    placeholder = {
                        Text("Ingrese el DNI")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = state.dniError != null,
                    supportingText = {
                        state.dniError?.let {
                            Text(it)
                        }
                    }
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Correo electrónico")
                    },
                    placeholder = {
                        Text("Ingrese el correo")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    isError = state.emailError != null,
                    supportingText = {
                        state.emailError?.let {
                            Text(it)
                        }
                    }
                )

                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Teléfono")
                    },
                    placeholder = {
                        Text("Ingrese el teléfono")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    isError = state.phoneError != null,
                    supportingText = {
                        state.phoneError?.let {
                            Text(it)
                        }
                    }
                )
            }
        }

        /*
         * =============================================
         * BOTÓN PRINCIPAL
         * =============================================
         */
        Button(
            onClick = onSave,
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {

            if (state.isSaving) {

                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )

            } else {

                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = null
                )
            }

            Text(
                text =
                    if (state.isSaving) {
                        "  Guardando..."
                    } else {
                        "  $buttonText"
                    },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}