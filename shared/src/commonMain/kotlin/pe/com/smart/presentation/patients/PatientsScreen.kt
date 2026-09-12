package pe.com.smart.presentation.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    /*
     * Primera carga y recargas
     * después de crear, editar o eliminar.
     */
    LaunchedEffect(refreshKey) {

        viewModel.loadPatients()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        when {

            uiState.isLoading &&
                    uiState.patients.isEmpty() -> {

                AppLoading(
                    message =
                        "Cargando pacientes..."
                )
            }

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

            else -> {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

                    item {

                        PatientSearchBar(
                            query =
                                uiState.searchQuery,

                            onQueryChange =
                                viewModel::onSearchChange
                        )
                    }

                    if (
                        uiState.patients.isEmpty()
                    ) {

                        item {

                            AppEmptyState(
                                title =
                                    "Sin pacientes",

                                message =
                                    if (
                                        uiState.searchQuery
                                            .isBlank()
                                    ) {
                                        "No hay pacientes registrados."
                                    } else {
                                        "No se encontraron coincidencias."
                                    }
                            )
                        }

                    } else {

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

                ExtendedFloatingActionButton(
                    onClick =
                        onCreatePatientClick,

                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(16.dp),

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
                                "Nuevo paciente"
                        )
                    }
                )
            }
        }
    }
}