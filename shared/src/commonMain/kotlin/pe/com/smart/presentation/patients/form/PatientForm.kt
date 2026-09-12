package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.com.smart.presentation.patients.form.PatientFormUiState

@Composable
fun PatientForm(
    state: PatientFormUiState,

    onNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDniChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,

    onSave: () -> Unit,

    buttonText: String,

    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        OutlinedTextField(
            value = state.name,
            onValueChange =
                onNameChange,
            label = {
                Text("Nombre")
            },
            isError =
                state.nameError != null,
            supportingText = {

                state.nameError?.let {

                    Text(it)
                }
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value =
                state.lastName,
            onValueChange =
                onLastNameChange,
            label = {
                Text("Apellido")
            },
            isError =
                state.lastNameError != null,
            supportingText = {

                state.lastNameError
                    ?.let {
                        Text(it)
                    }
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value =
                state.dni,
            onValueChange =
                onDniChange,
            label = {
                Text("DNI")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number
                ),
            isError =
                state.dniError != null,
            supportingText = {

                state.dniError?.let {
                    Text(it)
                }
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value =
                state.email,
            onValueChange =
                onEmailChange,
            label = {
                Text("Correo electrónico")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Email
                ),
            isError =
                state.emailError != null,
            supportingText = {

                state.emailError
                    ?.let {
                        Text(it)
                    }
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value =
                state.phone,
            onValueChange =
                onPhoneChange,
            label = {
                Text("Teléfono")
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Phone
                ),
            isError =
                state.phoneError != null,
            supportingText = {

                state.phoneError
                    ?.let {
                        Text(it)
                    }
            },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSave,
            enabled =
                !state.isSaving,
            modifier =
                Modifier.fillMaxWidth()
        ) {

            if (state.isSaving) {

                CircularProgressIndicator()
            } else {

                Text(
                    buttonText
                )
            }
        }

        state.error?.let {

            Text(
                text = it
            )
        }
    }
}