
package pe.com.smart.presentation.doctors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.com.smart.core.ui.AppEmptyState
import pe.com.smart.core.ui.AppError
import pe.com.smart.presentation.doctors.components.DoctorCard
import pe.com.smart.presentation.doctors.components.DoctorSearchBar

/**
 * Pantalla administrativa del listado de médicos.
 *
 * Conserva la integración con:
 * - DoctorsViewModel
 * - DoctorSearchBar
 * - DoctorCard
 * - AppNavigation
 */
@Composable
fun DoctorsScreen(
    refreshKey: Int = 0,
    onDoctorClick: (Int) -> Unit,
    onCreateDoctorClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoctorsViewModel = viewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, refreshKey) {
        viewModel.loadDoctors()
    }

    val isSearching = uiState.searchQuery.isNotBlank()
    val doctorCount = uiState.doctors.size

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.doctors.isEmpty() -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null &&
                    uiState.doctors.isEmpty() -> {

                AppError(
                    message = uiState.error
                        ?: "No se pudieron cargar los médicos.",
                    onRetry = viewModel::loadDoctors
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 20.dp,
                        bottom = 108.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DoctorsHeader()
                    }

                    item {
                        DoctorsSummary(
                            count = doctorCount,
                            isSearching = isSearching
                        )
                    }

                    item {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Buscar médico",
                                style = MaterialTheme
                                    .typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            DoctorSearchBar(
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
                                    text = "Actualizando médicos...",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme.onSurfaceVariant
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
                                    "Listado de médicos"
                                },
                                style = MaterialTheme
                                    .typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = doctorCount.toString(),
                                style = MaterialTheme
                                    .typography.labelLarge,
                                color = MaterialTheme
                                    .colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (uiState.doctors.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = if (isSearching) {
                                    "Sin resultados"
                                } else {
                                    "Sin médicos registrados"
                                },
                                message = if (isSearching) {
                                    "No se encontraron médicos que coincidan con tu búsqueda."
                                } else {
                                    "Los médicos registrados aparecerán aquí."
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.doctors,
                            key = { doctor ->
                                doctor.id
                            }
                        ) { doctor ->
                            DoctorCard(
                                doctor = doctor,
                                onClick = {
                                    onDoctorClick(doctor.id)
                                }
                            )
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onCreateDoctorClick,
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
                            text = "Nuevo médico",
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
 * Encabezado principal.
 */
@Composable
private fun DoctorsHeader() {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "Gestión de médicos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta y administra los profesionales médicos registrados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Resumen del listado.
 *
 * Si existe búsqueda, representa el número
 * de resultados filtrados.
 */
@Composable
private fun DoctorsSummary(
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
                        imageVector =
                            Icons.Outlined.MedicalServices,
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
                            "Médico registrado"

                        else ->
                            "Médicos registrados"
                    },
                    style = MaterialTheme
                        .typography.bodyMedium,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}
