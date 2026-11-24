
package pe.com.smart.presentation.patients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.patients.components.PatientCard
import pe.com.smart.presentation.patients.components.PatientSearchBar

/**
 * Pantalla administrativa de pacientes.
 *
 * Conserva:
 * - Búsqueda del ViewModel.
 * - Carga mediante refreshKey.
 * - Navegación al detalle.
 * - Registro de pacientes.
 */
@Composable
fun PatientsScreen(
    refreshKey: Int = 0,
    onPatientClick: (Int) -> Unit,
    onCreatePatientClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PatientsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, refreshKey) {
        viewModel.loadPatients()
    }

    val isSearching = uiState.searchQuery.isNotBlank()
    val patientCount = uiState.patients.size

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.patients.isEmpty() -> {
                AppLoading(
                    message = "Cargando pacientes..."
                )
            }

            uiState.error != null &&
                    uiState.patients.isEmpty() -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudieron cargar los pacientes.",
                    onRetry = viewModel::loadPatients
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp,
                        bottom = 108.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PatientsHeader()
                    }

                    item {
                        PatientsSummary(
                            count = patientCount,
                            isSearching = isSearching
                        )
                    }

                    item {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Buscar paciente",
                                style = MaterialTheme
                                    .typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            PatientSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange =
                                    viewModel::onSearchChange
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        item {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )

                                Text(
                                    text = "Actualizando pacientes...",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (uiState.error != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme
                                    .colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = uiState.error.orEmpty(),
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isSearching) {
                                    "Resultados de búsqueda"
                                } else {
                                    "Listado de pacientes"
                                },
                                style = MaterialTheme
                                    .typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = patientCount.toString(),
                                style = MaterialTheme
                                    .typography.labelLarge,
                                color = MaterialTheme
                                    .colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.patients.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = if (isSearching) {
                                    "Sin resultados"
                                } else {
                                    "Sin pacientes registrados"
                                },
                                message = if (isSearching) {
                                    "No se encontraron pacientes que coincidan con tu búsqueda."
                                } else {
                                    "Los pacientes registrados aparecerán aquí."
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.patients,
                            key = { patient -> patient.id }
                        ) { patient ->
                            PatientCard(
                                patient = patient,
                                onClick = {
                                    onPatientClick(patient.id)
                                }
                            )
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onCreatePatientClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                    icon = {
                        Icon(
                            imageVector =
                                Icons.Outlined.PersonAdd,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Nuevo paciente",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    containerColor = MaterialTheme
                        .colorScheme.primaryContainer,
                    contentColor = MaterialTheme
                        .colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Encabezado de la pantalla.
 */
@Composable
private fun PatientsHeader() {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Gestión de pacientes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta y administra la información de los pacientes registrados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tarjeta de resumen.
 *
 * Cuando hay una búsqueda activa, el total
 * corresponde al resultado filtrado.
 */
@Composable
private fun PatientsSummary(
    count: Int,
    isSearching: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(15.dp),
                color = colors.surface
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSearching) {
                            Icons.Outlined.SearchOff
                        } else {
                            Icons.Outlined.People
                        },
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme
                        .typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )

                Text(
                    text = when {
                        isSearching && count == 1 ->
                            "Resultado encontrado"

                        isSearching ->
                            "Resultados encontrados"

                        count == 1 ->
                            "Paciente registrado"

                        else ->
                            "Pacientes registrados"
                    },
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}
