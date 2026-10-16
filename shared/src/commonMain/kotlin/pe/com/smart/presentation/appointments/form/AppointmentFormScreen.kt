package pe.com.smart.presentation.appointments.form

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
import pe.com.smart.presentation.appointments.components.AppointmentForm


/*
 * =====================================================
 * PANTALLA DEL FORMULARIO DE CITAS
 * =====================================================
 *
 * Esta pantalla se utiliza tanto para:
 *
 * - registrar una cita nueva
 * - editar una cita existente
 */
@Composable
fun AppointmentFormScreen(

    // Si es null = nueva cita
    // Si tiene un número = editar cita
    appointmentId: Int?,

    // Se ejecuta cuando la cita se guarda correctamente
    onSaved: () -> Unit,

    // Se ejecuta cuando se confirma volver atrás
    onBackConfirmed: () -> Unit,

    // Sirve para detectar cuando el usuario presiona atrás
    backRequestKey: Int = 0,

    modifier: Modifier = Modifier
) {

    /*
     * =====================================================
     * VIEWMODEL
     * =====================================================
     *
     * Creamos un ViewModel diferente para:
     *
     * - crear cita
     * - editar cita
     */
    val viewModel: AppointmentFormViewModel =
        viewModel(
            key =
                if (appointmentId == null) {

                    "appointment_create"

                } else {

                    "appointment_edit_$appointmentId"
                }
        ) {

            AppointmentFormViewModel(
                appointmentId = appointmentId
            )
        }


    /*
     * =====================================================
     * ESTADO
     * =====================================================
     *
     * Observamos todos los datos del formulario.
     */
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()


    /*
     * Controla si mostramos el mensaje
     * para confirmar que queremos salir.
     */
    var showDiscardDialog by
    remember {
        mutableStateOf(false)
    }


    /*
     * =====================================================
     * BOTÓN ATRÁS
     * =====================================================
     *
     * Si hay cambios sin guardar,
     * pedimos confirmación.
     *
     * Si no hay cambios,
     * salimos directamente.
     */
    LaunchedEffect(
        backRequestKey
    ) {

        // 0 significa que aún no se pulsó volver
        if (backRequestKey == 0) {
            return@LaunchedEffect
        }


        // No permitimos salir mientras guarda
        if (uiState.isSaving) {
            return@LaunchedEffect
        }


        if (uiState.hasChanges) {

            // Hay cambios, preguntamos antes de salir
            showDiscardDialog =
                true

        } else {

            // No hay cambios, regresamos directamente
            onBackConfirmed()
        }
    }


    /*
     * =====================================================
     * CONTENIDO PRINCIPAL
     * =====================================================
     */
    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        /*
         * Si estamos editando una cita,
         * primero cargamos sus datos.
         */
        if (uiState.isLoading) {

            AppLoading(
                message =
                    "Cargando cita..."
            )

        } else {

            /*
             * =================================================
             * FORMULARIO
             * =================================================
             */
            AppointmentForm(

                // Datos actuales
                state =
                    uiState,

                // Indica si estamos editando
                isEditing =
                    viewModel.isEditing,

                // Cambios de cada campo
                onPatientIdChange =
                    viewModel::onPatientIdChange,

                onDoctorIdChange =
                    viewModel::onDoctorIdChange,

                onReasonChange =
                    viewModel::onReasonChange,

                onDateChange =
                    viewModel::onDateChange,

                onStartTimeChange =
                    viewModel::onStartTimeChange,

                onEndTimeChange =
                    viewModel::onEndTimeChange,

                onStatusChange =
                    viewModel::onStatusChange,


                /*
                 * Cuando pulsamos Guardar,
                 * llamamos al ViewModel.
                 */
                onSave = {

                    viewModel.saveAppointment(
                        onSuccess =
                            onSaved
                    )
                },


                /*
                 * Permitimos desplazarnos hacia abajo
                 * porque el formulario tiene varios campos.
                 */
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
     * DIÁLOGO PARA DESCARTAR CAMBIOS
     * =====================================================
     *
     * Aparece cuando el usuario intenta volver
     * y existen datos sin guardar.
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

                "Has ingresado información de la cita. ¿Deseas salir sin guardar?"
            },

        confirmText =
            "Descartar",

        cancelText =
            "Seguir editando",

        loadingText =
            "Saliendo...",

        // Icono de advertencia
        icon =
            Icons.Outlined.WarningAmber,

        // Salir elimina los cambios realizados
        destructive =
            true,

        isLoading =
            false,


        /*
         * Si confirma:
         * descartamos cambios y regresamos.
         */
        onConfirm = {

            showDiscardDialog =
                false

            onBackConfirmed()
        },


        /*
         * Si cancela:
         * sigue trabajando en el formulario.
         */
        onDismiss = {

            showDiscardDialog =
                false
        }
    )
}