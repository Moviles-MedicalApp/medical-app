package pe.com.smart.presentation.patients.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.patients.components.PatientForm

@Composable
fun PatientFormScreen(
    patientId: Int?,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {

    val viewModel: PatientFormViewModel =
        viewModel(
            key =
                if (patientId == null) {
                    "patient_create"
                } else {
                    "patient_edit_$patientId"
                }
        ) {

            PatientFormViewModel(
                patientId =
                    patientId
            )
        }

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        if (uiState.isLoading) {

            AppLoading(
                message =
                    "Cargando paciente..."
            )

        } else {

            PatientForm(
                state =
                    uiState,

                onNameChange =
                    viewModel::onNameChange,

                onLastNameChange =
                    viewModel::onLastNameChange,

                onDniChange =
                    viewModel::onDniChange,

                onEmailChange =
                    viewModel::onEmailChange,

                onPhoneChange =
                    viewModel::onPhoneChange,

                onSave = {

                    viewModel.savePatient(
                        onSuccess =
                            onSaved
                    )
                },

                buttonText =
                    if (
                        viewModel.isEditing
                    ) {
                        "Guardar cambios"
                    } else {
                        "Registrar paciente"
                    },

                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(20.dp)
            )
        }
    }
}