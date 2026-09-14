package pe.com.smart.presentation.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.patients.components.PatientCard
import pe.com.smart.presentation.patients.components.PatientSearchBar

@Composable
fun PatientsScreen(
    refreshKey: Int = 0,
    onPatientClick: (Int) -> Unit,
    onCreatePatientClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PatientsViewModel = viewModel()
) {

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        refreshKey
    ) {
        viewModel.loadPatients()
    }

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        when {

            /*
             * =================================================
             * LOADING
             * =================================================
             */
            uiState.isLoading &&
                    uiState.patients.isEmpty() -> {

                AppLoading(
                    message =
                        "Cargando pacientes..."
                )
            }

            /*
             * =================================================
             * ERROR
             * =================================================
             */
            uiState.error != null &&
                    uiState.patients.isEmpty() -> {

                AppError(
                    message =
                        uiState.error
                            ?: "No se pudieron cargar los pacientes.",

                    onRetry =
                        viewModel::loadPatients
                )
            }

            /*
             * =================================================
             * CONTENIDO
             * =================================================
             */
            else -> {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,

                            /*
                             * Espacio suficiente para
                             * FAB + BottomBar.
                             */
                            bottom = 132.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    /*
                     * =========================================
                     * BUSCADOR
                     * =========================================
                     */
                    item {

                        PatientSearchBar(
                            query =
                                uiState.searchQuery,

                            onQueryChange =
                                viewModel::onSearchChange
                        )
                    }

                    /*
                     * =========================================
                     * CONTADOR
                     * =========================================
                     */
                    if (uiState.patients.isNotEmpty()) {

                        item {

                            PatientsCount(
                                count =
                                    uiState.patients.size,

                                isSearching =
                                    uiState.searchQuery
                                        .isNotBlank()
                            )
                        }
                    }

                    /*
                     * =========================================
                     * VACÍO
                     * =========================================
                     */
                    if (uiState.patients.isEmpty()) {

                        item {

                            AppEmptyState(
                                title =
                                    if (
                                        uiState.searchQuery
                                            .isBlank()
                                    ) {
                                        "Sin pacientes"
                                    } else {
                                        "Sin resultados"
                                    },

                                message =
                                    if (
                                        uiState.searchQuery
                                            .isBlank()
                                    ) {
                                        "No hay pacientes registrados."
                                    } else {
                                        "No se encontraron pacientes que coincidan con la búsqueda."
                                    }
                            )
                        }

                    } else {

                        /*
                         * =====================================
                         * LISTADO
                         * =====================================
                         */
                        items(
                            items =
                                uiState.patients,

                            key = {
                                it.id
                            }
                        ) { patient ->

                            PatientCard(
                                patient =
                                    patient,

                                onClick = {

                                    onPatientClick(
                                        patient.id
                                    )
                                }
                            )
                        }
                    }
                }

                /*
                 * =================================================
                 * NUEVO PACIENTE
                 * =================================================
                 */
                ExtendedFloatingActionButton(
                    onClick =
                        onCreatePatientClick,

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
                                Icons.Outlined.PersonAdd,

                            contentDescription =
                                null
                        )
                    },

                    text = {

                        Text(
                            text =
                                "Nuevo paciente",

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


/*
 * =====================================================
 * CONTADOR
 * =====================================================
 */

@Composable
private fun PatientsCount(
    count: Int,
    isSearching: Boolean
) {

    Column(
        modifier =
            Modifier.padding(
                horizontal = 2.dp,
                vertical = 2.dp
            )
    ) {

        Text(
            text =
                when {

                    isSearching &&
                            count == 1 ->
                        "1 resultado encontrado"

                    isSearching ->
                        "$count resultados encontrados"

                    count == 1 ->
                        "1 paciente registrado"

                    else ->
                        "$count pacientes registrados"
                },

            style =
                MaterialTheme.typography.bodySmall,

            fontWeight =
                FontWeight.Medium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}