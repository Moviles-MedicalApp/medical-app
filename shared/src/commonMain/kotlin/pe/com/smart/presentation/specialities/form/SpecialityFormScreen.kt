package pe.com.smart.presentation.specialities.form

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
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.specialities.components.SpecialityForm

@Composable
fun SpecialityFormScreen(
    specialityId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {

    val viewModel: SpecialityFormViewModel =
        viewModel(
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

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDiscardDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(backRequestKey) {
        if (backRequestKey == 0) return@LaunchedEffect
        if (uiState.isSaving) return@LaunchedEffect

        if (uiState.hasChanges) {
            showDiscardDialog = true
        } else {
            onBackConfirmed()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        if (uiState.isLoading) {
            AppLoading(
                message = "Cargando especialidad..."
            )
        } else {
            SpecialityForm(
                state = uiState,
                isEditing = viewModel.isEditing,
                onNameChange = viewModel::onNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onDeletedChange = viewModel::onDeletedChange,
                onSave = {
                    viewModel.saveSpeciality(
                        onSuccess = onSaved
                    )
                },
                buttonText = if (viewModel.isEditing) {
                    "Guardar cambios"
                } else {
                    "Registrar especialidad"
                },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            )
        }
    }

    AppConfirmDialog(
        visible = showDiscardDialog,
        title = "Descartar cambios",
        message = if (viewModel.isEditing) {
            "Tienes cambios sin guardar. ¿Deseas salir sin guardar las modificaciones?"
        } else {
            "Has ingresado información de la especialidad. ¿Deseas salir sin guardar?"
        },
        confirmText = "Descartar",
        cancelText = "Seguir editando",
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
