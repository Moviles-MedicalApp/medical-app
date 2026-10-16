package pe.com.smart.presentation.appointments

// Elementos para organizar la pantalla
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

// Lista vertical
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Icono para crear una nueva cita
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

// Componentes visuales de Material Design
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Permite observar el estado del ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Crea automáticamente el ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Componentes generales que ya existen en el proyecto
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading

// Tarjeta que nosotros creamos para mostrar cada cita
import pe.com.smart.presentation.appointments.components.AppointmentCard


/*
 * =====================================================
 * PANTALLA PRINCIPAL DE CITAS
 * =====================================================
 *
 * Esta pantalla:
 * - carga las citas
 * - muestra una lista
 * - muestra errores
 * - permite abrir una cita
 * - permite registrar una nueva cita
 */
@Composable
fun AppointmentsScreen(

    // Se usará después para refrescar la lista
    refreshKey: Int = 0,

    // Acción cuando hacemos clic sobre una cita
    onAppointmentClick: (Int) -> Unit = {},

    // Acción del botón "Nueva cita"
    onCreateAppointmentClick: () -> Unit = {},

    modifier: Modifier = Modifier,

    // ViewModel encargado de manejar los datos
    viewModel: AppointmentsViewModel = viewModel()
) {

    /*
     * Observamos el estado del ViewModel.
     *
     * Cada vez que cambien las citas,
     * la pantalla se actualizará automáticamente.
     */
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()


    /*
     * Cuando entramos a esta pantalla,
     * solicitamos las citas al servidor.
     */
    LaunchedEffect(
        refreshKey
    ) {

        viewModel.loadAppointments()
    }


    /*
     * Caja principal de toda la pantalla.
     */
    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        /*
         * Dependiendo del estado,
         * mostramos carga, error o contenido.
         */
        when {

            /*
             * =================================================
             * CARGANDO
             * =================================================
             *
             * Si todavía no tenemos citas
             * y estamos cargando información.
             */
            uiState.isLoading &&
                    uiState.appointments.isEmpty() -> {

                AppLoading(
                    message =
                        "Cargando citas..."
                )
            }


            /*
             * =================================================
             * ERROR
             * =================================================
             *
             * Si ocurrió un error y no tenemos
             * ninguna cita para mostrar.
             */
            uiState.errorMessage != null &&
                    uiState.appointments.isEmpty() -> {

                AppError(
                    message =
                        uiState.errorMessage
                            ?: "No se pudieron cargar las citas.",

                    // Si pulsamos reintentar,
                    // vuelve a consultar la API
                    onRetry =
                        viewModel::loadAppointments
                )
            }


            /*
             * =================================================
             * CONTENIDO
             * =================================================
             */
            else -> {

                /*
                 * Lista vertical donde se mostrarán las citas.
                 */
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,

                            // Dejamos espacio para el botón inferior
                            bottom = 120.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    /*
                     * =========================================
                     * SI NO EXISTEN CITAS
                     * =========================================
                     */
                    if (
                        uiState.appointments.isEmpty()
                    ) {

                        item {

                            AppEmptyState(
                                title =
                                    "Sin citas",

                                message =
                                    "No hay citas médicas registradas."
                            )
                        }

                    } else {

                        /*
                         * =====================================
                         * LISTADO DE CITAS
                         * =====================================
                         *
                         * Recorremos todas las citas que
                         * llegaron desde la API.
                         */
                        items(
                            items =
                                uiState.appointments,

                            // Cada cita utiliza su ID como identificador
                            key = {
                                it.id
                            }
                        ) { appointment ->

                            /*
                             * Mostramos nuestra tarjeta.
                             */
                            AppointmentCard(
                                appointment =
                                    appointment,

                                // Al tocar la tarjeta,
                                // enviamos el ID de la cita
                                onClick = {

                                    onAppointmentClick(
                                        appointment.id
                                    )
                                }
                            )
                        }
                    }
                }


                /*
                 * =================================================
                 * BOTÓN NUEVA CITA
                 * =================================================
                 */
                ExtendedFloatingActionButton(
                    onClick =
                        onCreateAppointmentClick,

                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(
                                end = 16.dp,
                                bottom = 16.dp
                            ),

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Add,

                            contentDescription =
                                "Nueva cita"
                        )
                    },

                    text = {

                        Text(
                            text =
                                "Nueva cita",

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    },

                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer,

                    contentColor =
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}