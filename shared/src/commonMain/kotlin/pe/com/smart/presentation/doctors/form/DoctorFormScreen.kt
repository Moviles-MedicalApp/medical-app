
package pe.com.smart.presentation.doctors.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.domain.usecase.SpecialityUseCase

/**
 * Pantalla de creación y edición de médicos.
 *
 * Responsabilidades:
 * - Obtener los estados del ViewModel.
 * - Mostrar la carga inicial.
 * - Presentar el formulario.
 * - Gestionar la solicitud de regreso.
 * - Confirmar el descarte de cambios.
 */
@Composable
fun DoctorFormScreen(
    doctorId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {

    // =====================================================
    // VIEWMODEL
    // =====================================================

    val doctorUseCase: DoctorUseCase = koinInject()
    val specialityUseCase: SpecialityUseCase = koinInject()
    val formViewModel: DoctorFormViewModel = viewModel(
        key = if (doctorId == null) {
            "doctor_create"
        } else {
            "doctor_edit_$doctorId"
        }
    ) {
        DoctorFormViewModel(doctorId, doctorUseCase, specialityUseCase)
    }

    // =====================================================
    // UI STATE
    // =====================================================

    val uiState by formViewModel.uiState
        .collectAsStateWithLifecycle()

    val specialities by formViewModel.specialities
        .collectAsStateWithLifecycle()

    // =====================================================
    // DISCARD DIALOG
    // =====================================================

    var showDiscardDialog by remember(doctorId) {
        mutableStateOf(false)
    }

    // =====================================================
    // BACK REQUEST
    // =====================================================

    LaunchedEffect(backRequestKey) {
        if (backRequestKey == 0) {
            return@LaunchedEffect
        }

        // No permitir salir mientras guarda.
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
    // SCREEN CONTENT
    // =====================================================

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        if (uiState.isLoading) {

            AppLoading(
                message = if (formViewModel.isEditing) {
                    "Cargando información del médico..."
                } else {
                    "Preparando formulario..."
                }
            )

        } else {

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

                // =========================================
                // ERROR
                // =========================================

                if (!uiState.error.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme
                            .colorScheme.errorContainer
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme
                                .typography.bodyMedium,
                            color = MaterialTheme
                                .colorScheme.onErrorContainer
                        )
                    }
                }

                // =========================================
                // FORM
                // =========================================

                DoctorForm(
                    state = uiState,
                    specialities = specialities,
                    isEditing = formViewModel.isEditing,

                    onNameChange =
                        formViewModel::onNameChange,

                    onLastNameChange =
                        formViewModel::onLastNameChange,

                    onEmailChange =
                        formViewModel::onEmailChange,

                    onPhoneChange =
                        formViewModel::onPhoneChange,

                    onSpecialityChange =
                        formViewModel::onSpecialityChange,

                    onSave = {
                        formViewModel.saveDoctor(
                            onSuccess = onSaved
                        )
                    },

                    buttonText =
                        if (formViewModel.isEditing) {
                            "Guardar cambios"
                        } else {
                            "Registrar médico"
                        },

                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // =====================================================
    // CONFIRM DISCARD
    // =====================================================

    AppConfirmDialog(
        visible = showDiscardDialog,

        title = "Descartar cambios",

        message = if (formViewModel.isEditing) {
            "Tienes modificaciones sin guardar. ¿Deseas salir sin guardar los cambios?"
        } else {
            "Has ingresado información del médico. ¿Deseas salir sin guardar?"
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
