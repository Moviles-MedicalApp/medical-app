
package pe.com.smart.presentation.specialities.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading

/**
 * Pantalla para registrar y editar especialidades.
 *
 * Mantiene la navegación y el control
 * de cambios pendientes.
 */
@Composable
fun SpecialityFormScreen(
    specialityId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {
    val formViewModel: SpecialityFormViewModel = viewModel(
        key = if (specialityId == null) {
            "speciality_create"
        } else {
            "speciality_edit_$specialityId"
        }
    ) {
        SpecialityFormViewModel(
            specialityId = specialityId
        )
    }

    val uiState by formViewModel.uiState
        .collectAsStateWithLifecycle()

    var showDiscardDialog by remember(specialityId) {
        mutableStateOf(false)
    }

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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                AppLoading(
                    message = if (formViewModel.isEditing) {
                        "Cargando especialidad..."
                    } else {
                        "Preparando formulario..."
                    }
                )
            }

            formViewModel.isEditing &&
                    !uiState.error.isNullOrBlank() &&
                    !uiState.hasChanges &&
                    uiState.name.isBlank() -> {

                AppError(
                    message = uiState.error
                        ?: "No se pudo cargar la especialidad.",
                    onRetry = {
                        formViewModel.retryLoad()
                    }
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = 16.dp,
                            vertical = 20.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    SpecialityForm(
                        state = uiState,
                        isEditing = formViewModel.isEditing,
                        onNameChange =
                            formViewModel::onNameChange,
                        onDescriptionChange =
                            formViewModel::onDescriptionChange,
                        onDeletedChange =
                            formViewModel::onDeletedChange,
                        onSave = {
                            formViewModel.saveSpeciality(
                                onSuccess = onSaved
                            )
                        },
                        buttonText =
                            if (formViewModel.isEditing) {
                                "Guardar cambios"
                            } else {
                                "Registrar especialidad"
                            },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Descartar cambios",
        message = if (formViewModel.isEditing) {
            "Tienes modificaciones sin guardar. ¿Deseas salir sin guardar los cambios?"
        } else {
            "Has ingresado información de la especialidad. ¿Deseas salir sin guardar?"
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
