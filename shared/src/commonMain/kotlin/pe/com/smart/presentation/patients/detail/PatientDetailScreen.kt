package pe.com.smart.presentation.patients.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.core.ui.InfoRow

@Composable
fun PatientDetailScreen(
    patientId: Int,
    refreshKey: Int = 0,
    onEditClick: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel: PatientDetailViewModel =
        viewModel(
            key =
                "patient_detail_$patientId"
        ) {

            PatientDetailViewModel(
                patientId =
                    patientId
            )
        }

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDeleteDialog by
    remember {
        mutableStateOf(false)
    }

    /*
     * Primera carga y recarga
     * después de editar.
     */
    LaunchedEffect(
        patientId,
        refreshKey
    ) {

        viewModel.loadPatient()
    }

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        when {

            uiState.isLoading &&
                    uiState.patient == null -> {

                AppLoading(
                    message =
                        "Cargando paciente..."
                )
            }

            uiState.error != null &&
                    uiState.patient == null -> {

                AppError(
                    message =
                        uiState.error
                            ?: "No se pudo cargar el paciente.",

                    onRetry =
                        viewModel::loadPatient
                )
            }

            uiState.patient != null -> {

                val patient =
                    uiState.patient!!

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(20.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    Text(
                        text =
                            patient.fullName,

                        style =
                            MaterialTheme.typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    InfoRow(
                        icon =
                            Icons.Outlined.Badge,

                        text =
                            "DNI: ${patient.dni}"
                    )

                    InfoRow(
                        icon =
                            Icons.Outlined.Email,

                        text =
                            patient.email
                    )

                    InfoRow(
                        icon =
                            Icons.Outlined.Phone,

                        text =
                            patient.phone
                    )

                    /*
                     * Si falla una recarga pero ya
                     * tenemos datos anteriores,
                     * mantenemos el contenido visible.
                     */
                    uiState.error?.let { error ->

                        Text(
                            text = error,

                            style =
                                MaterialTheme.typography
                                    .bodyMedium,

                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Button(
                        onClick = {

                            onEditClick(
                                patient.id
                            )
                        },

                        enabled =
                            !uiState.isDeleting,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Edit,

                            contentDescription =
                                null
                        )

                        Text(
                            text =
                                "  Editar paciente"
                        )
                    }

                    OutlinedButton(
                        onClick = {

                            showDeleteDialog =
                                true
                        },

                        enabled =
                            !uiState.isDeleting,

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Delete,

                            contentDescription =
                                null
                        )

                        Text(
                            text =
                                if (
                                    uiState.isDeleting
                                ) {
                                    "  Eliminando..."
                                } else {
                                    "  Eliminar paciente"
                                }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {

                if (!uiState.isDeleting) {

                    showDeleteDialog =
                        false
                }
            },

            title = {

                Text(
                    text =
                        "Eliminar paciente"
                )
            },

            text = {

                Text(
                    text =
                        "¿Está seguro de eliminar este paciente? Esta acción no se puede deshacer."
                )
            },

            confirmButton = {

                TextButton(
                    enabled =
                        !uiState.isDeleting,

                    onClick = {

                        viewModel.deletePatient(

                            onSuccess = {

                                showDeleteDialog =
                                    false

                                onDeleted()
                            }
                        )
                    }
                ) {

                    Text(
                        text =
                            if (
                                uiState.isDeleting
                            ) {
                                "Eliminando..."
                            } else {
                                "Eliminar"
                            },

                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    enabled =
                        !uiState.isDeleting,

                    onClick = {

                        showDeleteDialog =
                            false
                    }
                ) {

                    Text(
                        text =
                            "Cancelar"
                    )
                }
            }
        )
    }
}