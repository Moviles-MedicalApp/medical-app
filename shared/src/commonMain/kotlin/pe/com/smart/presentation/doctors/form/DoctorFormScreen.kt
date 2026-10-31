package pe.com.smart.presentation.doctors.form

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

@Composable
fun DoctorFormScreen(
    doctorId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {
    val formViewModel: DoctorFormViewModel = viewModel(
        key = if (doctorId == null) {
            "doctor_create"
        } else {
            "doctor_edit_$doctorId"
        }
    ) {
        DoctorFormViewModel(doctorId)
    }

    val uiState by formViewModel.uiState.collectAsStateWithLifecycle()
    val specialities by formViewModel.specialities.collectAsStateWithLifecycle()

    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(backRequestKey) {
        if (backRequestKey == 0 || uiState.isSaving) {
            return@LaunchedEffect
        }

        if (uiState.hasChanges) {
            showDiscardDialog = true
        } else {
            onBackConfirmed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            AppLoading(message = "Cargando médico...")
        } else {
            DoctorForm(
                state = uiState,
                specialities = specialities,
                isEditing = formViewModel.isEditing,
                onNameChange = formViewModel::onNameChange,
                onLastNameChange = formViewModel::onLastNameChange,
                onEmailChange = formViewModel::onEmailChange,
                onPhoneChange = formViewModel::onPhoneChange,
                onSpecialityChange = formViewModel::onSpecialityChange,
                onSave = {
                    formViewModel.saveDoctor(onSuccess = onSaved)
                },
                buttonText = if (formViewModel.isEditing) {
                    "Guardar cambios"
                } else {
                    "Registrar médico"
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
        message = if (formViewModel.isEditing) {
            "Tienes cambios sin guardar. ¿Deseas salir sin guardar las modificaciones?"
        } else {
            "Has ingresado información del médico. ¿Deseas salir sin guardar?"
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
