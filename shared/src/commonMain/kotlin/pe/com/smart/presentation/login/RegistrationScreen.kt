
package pe.com.smart.presentation.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Registro público de pacientes.
 *
 * Mantiene las funciones onRegistered y onBack
 * utilizadas por la navegación existente.
 */
@Composable
fun RegistrationScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.uiState
        .collectAsStateWithLifecycle()

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        IconButton(
            onClick = onBack,
            enabled = !state.isLoading,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver a iniciar sesión",
                tint = colors.onSurface
            )
        }

        // Encabezado.
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = colors.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Regístrate como paciente para gestionar tus consultas y reservar citas médicas.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        // Datos para iniciar sesión.
        RegistrationSection(
            title = "Datos de acceso",
            description = "Información con la que ingresarás a MedicalApp."
        ) {
            RegistrationInput(
                label = "Usuario *",
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                icon = Icons.Outlined.Person,
                error = state.usernameError,
                enabled = !state.isLoading
            )

            RegistrationInput(
                label = "Correo electrónico *",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                icon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                error = state.emailError,
                enabled = !state.isLoading
            )
        }

        // Información del paciente.
        RegistrationSection(
            title = "Información personal",
            description = "Completa los datos de tu perfil de paciente."
        ) {
            RegistrationInput(
                label = "Nombres *",
                value = state.name,
                onValueChange = viewModel::onNameChange,
                icon = Icons.Outlined.Person,
                error = state.nameError,
                enabled = !state.isLoading
            )

            RegistrationInput(
                label = "Apellidos *",
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                icon = Icons.Outlined.Person,
                error = state.lastNameError,
                enabled = !state.isLoading
            )

            RegistrationInput(
                label = "DNI *",
                value = state.dni,
                onValueChange = viewModel::onDniChange,
                icon = Icons.Outlined.Badge,
                keyboardType = KeyboardType.Number,
                error = state.dniError,
                enabled = !state.isLoading,
                supportingHint = "8 dígitos"
            )

            RegistrationInput(
                label = "Teléfono (opcional)",
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                icon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                error = state.phoneError,
                enabled = !state.isLoading
            )
        }

        // Contraseña.
        RegistrationSection(
            title = "Seguridad",
            description = "Define una contraseña para proteger tu cuenta."
        ) {
            RegistrationInput(
                label = "Contraseña *",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                icon = Icons.Outlined.Lock,
                isPassword = true,
                error = state.passwordError,
                enabled = !state.isLoading,
                supportingHint = "Entre 8 y 72 caracteres"
            )

            RegistrationInput(
                label = "Confirmar contraseña *",
                value = state.confirmation,
                onValueChange =
                    viewModel::onConfirmationChange,
                icon = Icons.Outlined.Lock,
                isPassword = true,
                error = state.confirmationError,
                enabled = !state.isLoading
            )
        }

        // Error devuelto por el servicio.
        state.error?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.errorContainer
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onErrorContainer
                )
            }
        }

        Button(
            onClick = {
                viewModel.register(onRegistered)
            },
            enabled = state.canSubmit && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(19.dp),
                    color = colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta")
            }
        }

        Text(
            text = if (state.canSubmit) {
                "Todo listo para crear tu cuenta."
            } else {
                "Completa los campos obligatorios marcados con * para continuar."
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        TextButton(
            onClick = onBack,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }

        Text(
            text = "Tu cuenta tendrá acceso exclusivamente como paciente.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
private fun RegistrationSection(
    title: String,
    description: String,
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
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
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
private fun RegistrationInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    error: String? = null,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingHint: String? = null
) {
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        }
                    )
                }
            }
        } else {
            null
        },
        visualTransformation = if (
            isPassword && !passwordVisible
        ) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        isError = error != null,
        supportingText = if (error != null) {
            {
                Text(error)
            }
        } else if (supportingHint != null) {
            {
                Text(supportingHint)
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) {
                KeyboardType.Password
            } else {
                keyboardType
            },
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
