
package pe.com.smart.presentation.patients.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.koinInject
import pe.com.smart.domain.usecase.PatientUseCase
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.patients.components.PatientForm

/**
 * Pantalla de registro y edición de pacientes.
 *
 * Controla:
 * - Estado del ViewModel.
 * - Carga de información.
 * - Confirmación de salida.
 * - Guardado de paciente.
 */
@Composable
fun PatientFormScreen(
    patientId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {
    // =====================================================
    // VIEWMODEL
    // =====================================================

    val patientUseCase: PatientUseCase = koinInject()
    val viewModel: PatientFormViewModel = viewModel(
        key = if (patientId == null) {
            "patient_create"
        } else {
            "patient_edit_$patientId"
        }
    ) {
        PatientFormViewModel(patientId, patientUseCase)
    }

    // =====================================================
    // UI STATE
    // =====================================================

    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    var showDiscardDialog by remember(patientId) {
        mutableStateOf(false)
    }

    // =====================================================
    // BACK NAVIGATION
    // =====================================================

    LaunchedEffect(backRequestKey) {
        if (backRequestKey == 0) {
            return@LaunchedEffect
        }

        if (uiState.isSaving || uiState.isLoading) {
            return@LaunchedEffect
        }

        if (uiState.hasChanges) {
            showDiscardDialog = true
        } else {
            onBackConfirmed()
        }
    }

    // =====================================================
    // CONTENT
    // =====================================================

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                AppLoading(
                    message = if (viewModel.isEditing) {
                        "Cargando información del paciente..."
                    } else {
                        "Preparando formulario..."
                    }
                )
            }

            uiState.error != null && viewModel.isEditing -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar el paciente.",
                    onRetry = {
                        // El ViewModel carga el paciente
                        // durante su inicialización.
                        // Para reintentar desde esta pantalla,
                        // debe exponerse un método público
                        // de recarga en el ViewModel.
                    }
                )
            }

            else -> {
                PatientForm(
                    state = uiState,
                    isEditing = viewModel.isEditing,

                    onNameChange = viewModel::onNameChange,
                    onLastNameChange =
                        viewModel::onLastNameChange,
                    onDniChange = viewModel::onDniChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPhoneChange = viewModel::onPhoneChange,

                    onSave = {
                        viewModel.savePatient(
                            onSuccess = onSaved
                        )
                    },

                    buttonText = if (viewModel.isEditing) {
                        "Guardar cambios"
                    } else {
                        "Registrar paciente"
                    },

                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = 16.dp,
                            vertical = 20.dp
                        )
                )
            }
        }
    }

    // =====================================================
    // DISCARD CHANGES
    // =====================================================

    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Descartar cambios",
        message = if (viewModel.isEditing) {
            "Tienes modificaciones sin guardar. ¿Deseas salir sin guardar los cambios?"
        } else {
            "Has ingresado información del paciente. ¿Deseas salir sin guardar?"
        },
        confirmText = "Descartar",
        cancelText = "Continuar editando",
        loadingText = "Saliendo...",
        icon = Icons.Outlined.WarningAmber,
        destructive = true,
        isLoading = false,
        onConfirm = {
            showDiscardDialog = false
            onBackConfirmed()
        },
        onDismiss = {
            showDiscardDialog = false
        }
    )
}
