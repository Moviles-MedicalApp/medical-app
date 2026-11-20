
package pe.com.smart.presentation.doctors.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.koinInject
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.domain.model.Doctor

/**
 * Pantalla de detalle de médico.
 *
 * Conserva:
 * - Carga mediante doctorId y refreshKey.
 * - Navegación a edición.
 * - Creación o vinculación de cuenta.
 * - Eliminación con confirmación.
 */
@Composable
fun DoctorDetailScreen(
    doctorId: Int,
    refreshKey: Int = 0,
    onEdit: (Int) -> Unit,
    onProvisionAccount: (Doctor) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    doctorUseCase: DoctorUseCase = koinInject(),
    viewModel: DoctorDetailViewModel = viewModel(
        key = "doctor_detail_$doctorId"
    ) {
        DoctorDetailViewModel(doctorUseCase)
    }
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by remember(doctorId) {
        mutableStateOf(false)
    }

    LaunchedEffect(
        doctorId,
        refreshKey,
        viewModel
    ) {
        viewModel.loadDoctor(doctorId)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.doctor == null -> {
                AppLoading(
                    message = "Cargando médico..."
                )
            }

            uiState.error != null &&
                    uiState.doctor == null -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar el médico.",
                    onRetry = {
                        viewModel.loadDoctor(doctorId)
                    }
                )
            }

            uiState.doctor != null -> {
                val doctor = uiState.doctor!!

                DoctorDetailContent(
                    doctor = doctor,
                    isDeleting = uiState.isDeleting,
                    error = uiState.error,
                    onEditClick = {
                        onEdit(doctor.id)
                    },
                    onProvisionAccount = {
                        onProvisionAccount(doctor)
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    AppConfirmDialog(
        visible = showDeleteDialog,
        title = "Eliminar médico",
        message =
            "¿Estás seguro de que deseas eliminar este médico? Esta acción no se puede deshacer.",
        confirmText = "Eliminar",
        cancelText = "Cancelar",
        loadingText = "Eliminando...",
        icon = Icons.Outlined.Delete,
        destructive = true,
        isLoading = uiState.isDeleting,
        onConfirm = {
            if (!uiState.isDeleting) {
                viewModel.deleteDoctor(
                    onSuccess = {
                        showDeleteDialog = false
                        onDeleted()
                    }
                )
            }
        },
        onDismiss = {
            if (!uiState.isDeleting) {
                showDeleteDialog = false
            }
        }
    )
}

/**
 * Contenido principal del detalle médico.
 */
@Composable
private fun DoctorDetailContent(
    doctor: Doctor,
    isDeleting: Boolean,
    error: String?,
    onEditClick: () -> Unit,
    onProvisionAccount: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
        DoctorProfileCard(doctor)

        if (!error.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme
                    .colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme
                        .colorScheme.onErrorContainer
                )
            }
        }

        DoctorContactCard(doctor)

        DoctorActionsSection(
            isDeleting = isDeleting,
            onEditClick = onEditClick,
            onProvisionAccount = onProvisionAccount,
            onDeleteClick = onDeleteClick
        )

        Spacer(Modifier.size(12.dp))
    }
}

/**
 * Tarjeta del perfil profesional.
 */
@Composable
private fun DoctorProfileCard(
    doctor: Doctor
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doctorInitials(
                                doctor.fullName
                            ),
                            style = MaterialTheme
                                .typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = doctor.fullName,
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Profesional médico",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            DoctorInformationRow(
                icon = Icons.Outlined.LocalHospital,
                label = "Especialidad médica",
                value = doctor.specialityName
            )
        }
    }
}

/**
 * Datos de contacto.
 */
@Composable
private fun DoctorContactCard(
    doctor: Doctor
) {
    DoctorSectionCard(
        title = "Información de contacto"
    ) {
        DoctorInformationRow(
            icon = Icons.Outlined.Email,
            label = "Correo electrónico",
            value = doctor.email.orEmpty()
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        DoctorInformationRow(
            icon = Icons.Outlined.Phone,
            label = "Teléfono",
            value = doctor.phone.orEmpty()
        )
    }
}

/**
 * Acciones administrativas del médico.
 */
@Composable
private fun DoctorActionsSection(
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onProvisionAccount: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Acciones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DoctorSectionCard(
            title = "Cuenta de acceso"
        ) {
            Text(
                text =
                    "Configura la cuenta que permite al profesional ingresar a MedicalApp.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme
                    .colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onProvisionAccount,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Crear o vincular cuenta",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Button(
            onClick = onEditClick,
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Editar médico",
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onDeleteClick,
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor =
                    MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = if (isDeleting) {
                    "Eliminando..."
                } else {
                    "Eliminar médico"
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Contenedor reutilizable de información.
 */
@Composable
private fun DoctorSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme
                    .typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            HorizontalDivider(
                color = colors.outlineVariant
            )

            content()
        }
    }
}

/**
 * Fila de información del profesional.
 */
@Composable
private fun DoctorInformationRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
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
                    modifier = Modifier.size(22.dp),
                    tint = colors.primary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )

            Text(
                text = value.ifBlank {
                    "No especificado"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Iniciales del médico para su avatar.
 */
private fun doctorInitials(
    fullName: String
): String {
    val words = fullName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        words.isEmpty() -> "M"

        words.size == 1 ->
            words.first()
                .take(2)
                .uppercase()

        else -> buildString {
            append(
                words.first()
                    .first()
                    .uppercaseChar()
            )

            append(
                words.last()
                    .first()
                    .uppercaseChar()
            )
        }
    }
}
