
package pe.com.smart.presentation.specialities.detail

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MedicalServices
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.domain.model.Speciality

/**
 * Detalle de una especialidad médica.
 *
 * Conserva:
 * - Carga por specialityId.
 * - Recarga mediante refreshKey.
 * - Edición.
 * - Eliminación con confirmación.
 */
@Composable
fun SpecialityDetailScreen(
    specialityId: Int,
    refreshKey: Int = 0,
    onEditClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SpecialityDetailViewModel = viewModel(
        key = "speciality_detail_$specialityId"
    ) {
        SpecialityDetailViewModel(
            specialityId = specialityId
        )
    }

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by remember(specialityId) {
        mutableStateOf(false)
    }

    LaunchedEffect(
        specialityId,
        refreshKey,
        viewModel
    ) {
        viewModel.loadSpeciality()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.speciality == null -> {
                AppLoading(
                    message = "Cargando especialidad..."
                )
            }

            uiState.error != null &&
                    uiState.speciality == null -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar la especialidad.",
                    onRetry = viewModel::loadSpeciality
                )
            }

            uiState.speciality != null -> {
                val speciality = uiState.speciality!!

                SpecialityDetailContent(
                    speciality = speciality,
                    isDeleting = uiState.isDeleting,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onEditClick = {
                        onEditClick(speciality.id)
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
        title = "Eliminar especialidad",
        message = "¿Estás seguro de que deseas eliminar esta especialidad? Esta acción no se puede deshacer.",
        confirmText = "Eliminar",
        cancelText = "Cancelar",
        loadingText = "Eliminando...",
        icon = Icons.Outlined.Delete,
        destructive = true,
        isLoading = uiState.isDeleting,
        onConfirm = {
            if (!uiState.isDeleting) {
                viewModel.deleteSpeciality(
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
 * Contenido principal del detalle.
 */
@Composable
private fun SpecialityDetailContent(
    speciality: Speciality,
    isDeleting: Boolean,
    isLoading: Boolean,
    error: String?,
    onEditClick: () -> Unit,
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
        SpecialityProfileCard(
            speciality = speciality
        )

        if (!error.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme
                    .colorScheme.errorContainer
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

        SpecialityDescriptionCard(
            description = speciality.description
        )

        SpecialityActions(
            isDeleting = isDeleting,
            isLoading = isLoading,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )

        Spacer(
            modifier = Modifier.size(12.dp)
        )
    }
}

/**
 * Tarjeta de perfil y estado.
 */
@Composable
private fun SpecialityProfileCard(
    speciality: Speciality
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            colors.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
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
                    shape = RoundedCornerShape(18.dp),
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = colors.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = speciality.name,
                        style = MaterialTheme
                            .typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )

                    Text(
                        text = "Especialidad médica",
                        style = MaterialTheme
                            .typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Estado de la especialidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                SpecialityStatus(
                    isActive = !speciality.deleted
                )
            }
        }
    }
}

/**
 * Indicador de estado.
 */
@Composable
private fun SpecialityStatus(
    isActive: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) {
            colors.primaryContainer
        } else {
            colors.errorContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isActive) {
                    colors.onPrimaryContainer
                } else {
                    colors.onErrorContainer
                }
            )

            Text(
                text = if (isActive) {
                    "Activa"
                } else {
                    "Inactiva"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) {
                    colors.onPrimaryContainer
                } else {
                    colors.onErrorContainer
                }
            )
        }
    }
}

/**
 * Tarjeta de descripción.
 */
@Composable
private fun SpecialityDescriptionCard(
    description: String
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Text(
                    text = "Descripción",
                    style = MaterialTheme
                        .typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                color = colors.outlineVariant
            )

            Text(
                text = description.ifBlank {
                    "Esta especialidad no tiene una descripción registrada."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (description.isBlank()) {
                    colors.onSurfaceVariant
                } else {
                    colors.onSurface
                }
            )
        }
    }
}

/**
 * Acciones administrativas.
 */
@Composable
private fun SpecialityActions(
    isDeleting: Boolean,
    isLoading: Boolean,
    onEditClick: () -> Unit,
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

        Button(
            onClick = onEditClick,
            enabled = !isDeleting && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "Editar especialidad",
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onDeleteClick,
            enabled = !isDeleting && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme
                        .colorScheme.error
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = if (isDeleting) {
                    "Eliminando..."
                } else {
                    "Eliminar especialidad"
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
