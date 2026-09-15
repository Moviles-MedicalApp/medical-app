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
import pe.com.smart.core.ui.AppConfirmDialog
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.patients.components.PatientForm

@Composable
fun PatientFormScreen(
    patientId: Int?,
    onSaved: () -> Unit,
    onBackConfirmed: () -> Unit,
    backRequestKey: Int = 0,
    modifier: Modifier = Modifier
) {

    /*
     * =====================================================
     * VIEWMODEL
     * =====================================================
     */

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
                patientId = patientId
            )
        }

    /*
     * =====================================================
     * ESTADO
     * =====================================================
     */

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    /*
     * Control del diálogo para
     * descartar cambios.
     */
    var showDiscardDialog by
    remember {
        mutableStateOf(false)
    }

    /*
     * =====================================================
     * SOLICITUD DE VOLVER
     * =====================================================
     *
     * AppNavigation incrementa backRequestKey
     * cuando el usuario pulsa la flecha atrás.
     *
     * Aquí decidimos:
     *
     * - sin cambios -> volver directamente
     * - con cambios -> pedir confirmación
     */

    LaunchedEffect(
        backRequestKey
    ) {

        /*
         * 0 es el estado inicial.
         * No hacemos nada.
         */
        if (backRequestKey == 0) {
            return@LaunchedEffect
        }

        /*
         * No permitimos salir mientras
         * se está guardando.
         */
        if (uiState.isSaving) {
            return@LaunchedEffect
        }

        if (uiState.hasChanges) {

            showDiscardDialog =
                true

        } else {

            onBackConfirmed()
        }
    }

    /*
     * =====================================================
     * CONTENIDO
     * =====================================================
     */

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        /*
         * =================================================
         * LOADING
         * =================================================
         */

        if (uiState.isLoading) {

            AppLoading(
                message =
                    "Cargando paciente..."
            )

        } else {

            /*
             * =================================================
             * FORMULARIO
             * =================================================
             */

            PatientForm(
                state =
                    uiState,

                /*
                 * Permite que PatientForm sepa
                 * si está creando o editando.
                 */
                isEditing =
                    viewModel.isEditing,

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
                    if (viewModel.isEditing) {

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
                        .padding(
                            20.dp
                        )
            )
        }
    }

    /*
     * =====================================================
     * DESCARTAR CAMBIOS
     * =====================================================
     */

    AppConfirmDialog(
        visible =
            showDiscardDialog,

        title =
            "Descartar cambios",

        message =
            if (viewModel.isEditing) {

                "Tienes cambios sin guardar. ¿Deseas salir sin guardar las modificaciones?"

            } else {

                "Has ingresado información del paciente. ¿Deseas salir sin guardar?"
            },

        confirmText =
            "Descartar",

        cancelText =
            "Seguir editando",

        loadingText =
            "Saliendo...",

        icon =
            Icons.Outlined.WarningAmber,

        /*
         * Descartar cambios implica pérdida
         * de información, por eso utilizamos
         * estilo destructivo.
         */
        destructive =
            true,

        isLoading =
            false,

        onConfirm = {

            showDiscardDialog =
                false

            onBackConfirmed()
        },

        onDismiss = {

            showDiscardDialog =
                false
        }
    )
}