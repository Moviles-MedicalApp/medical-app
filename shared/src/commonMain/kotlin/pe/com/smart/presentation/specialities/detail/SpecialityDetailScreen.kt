package pe.com.smart.presentation.specialities.detail

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.Speciality

@Composable
fun SpecialityDetailScreen(
    specialityId: Int,
    refreshKey: Int = 0,
    onEditClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel: SpecialityDetailViewModel =
        viewModel(
            key = "speciality_detail_$specialityId"
        ) {
            SpecialityDetailViewModel(
                specialityId = specialityId
            )
        }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(specialityId, refreshKey) {
        viewModel.loadSpeciality()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        when {
            uiState.isLoading && uiState.speciality == null -> {
                AppLoading(
                    message = "Cargando especialidad..."
                )
            }

            uiState.error != null && uiState.speciality == null -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar la especialidad.",
                    onRetry = viewModel::loadSpeciality
                )
            }

            uiState.speciality != null -> {
                SpecialityDetailContent(
                    speciality = uiState.speciality!!,
                    doctors = uiState.doctors,
                    isDeleting = uiState.isDeleting,
                    error = uiState.error,
                    onEditClick = {
                        onEditClick(uiState.speciality!!.id)
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
            viewModel.deleteSpeciality(
                onSuccess = {
                    showDeleteDialog = false
                    onDeleted()
                }
            )
        },
        onDismiss = {
            showDeleteDialog = false
        }
    )
}

@Composable
private fun SpecialityDetailContent(
    speciality: Speciality,
    doctors: List<Doctor>,
    isDeleting: Boolean,
    error: String?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /*
         * =================================================
         * 1. TARJETA PRINCIPAL (DATOS DE LA ESPECIALIDAD)
         * =================================================
         */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = speciality.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (speciality.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (speciality.active) "Estado: Activa" else "Estado: Inactiva",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (speciality.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (speciality.description.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = speciality.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        /*
         * =================================================
         * 2. MÉDICOS ASIGNADOS
         * =================================================
         */
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Médicos asignados (${doctors.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (doctors.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "No hay médicos asignados a esta especialidad.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                doctors.forEach { doctor ->
                    DoctorItemCard(doctor = doctor)
                }
            }
        }

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        /*
         * =================================================
         * 3. ACCIONES
         * =================================================
         */
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Acciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onEditClick,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editar especialidad",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onDeleteClick,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDeleting) "Eliminando..." else "Eliminar especialidad",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DoctorItemCard(
    doctor: Doctor,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {

                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = doctor.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = doctor.cmp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = doctor.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = doctor.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
