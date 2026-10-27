
package pe.com.smart.presentation.doctors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.presentation.doctors.detail.DoctorDetailViewModel

@Composable
fun DoctorDetailScreen(
    doctorId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoctorDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(doctorId) {
        viewModel.loadDoctor(doctorId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver")
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null && uiState.doctor == null -> {
                Text(
                    text = uiState.error
                        ?: "No se pudo cargar el médico.",
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.loadDoctor(doctorId) }) {
                    Text("Reintentar")
                }
            }

            uiState.doctor != null -> {
                val doctor = uiState.doctor!!

                Text(
                    text = "Detalle del médico",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = doctor.fullName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text("Especialidad: ${doctor.specialityName}")
                Text("Correo: ${doctor.email ?: "No registrado"}")
                Text("Teléfono: ${doctor.phone ?: "No registrado"}")

                if (uiState.error != null) {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onEdit(doctor.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar médico")
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.isDeleting) {
                            "Eliminando..."
                        } else {
                            "Eliminar médico"
                        }
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteDialog = false
                        },
                        title = {
                            Text("Eliminar médico")
                        },
                        text = {
                            Text(
                                "¿Seguro que deseas eliminar a ${doctor.fullName}?"
                            )
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !uiState.isDeleting,
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteDoctor(onDeleted)
                                }
                            ) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }

            else -> {
                Text("No se encontró información del médico.")
            }
        }
    }
}
