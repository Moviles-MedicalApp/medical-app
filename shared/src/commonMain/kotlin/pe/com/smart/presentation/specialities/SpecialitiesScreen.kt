
package pe.com.smart.presentation.specialities

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MedicalServices
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
import pe.com.smart.core.ui.AppLoading
import pe.com.smart.presentation.specialities.components.SpecialityCard
import pe.com.smart.presentation.specialities.components.SpecialitySearchBar

/**
 * Listado de especialidades médicas.
 *
 * Mantiene:
 * - Búsqueda mediante SpecialitiesViewModel.
 * - Recarga mediante refreshKey.
 * - Navegación al detalle.
 * - Registro opcional de especialidades.
 */
@Composable
fun SpecialitiesScreen(
    refreshKey: Int = 0,
    onSpecialityClick: ((Int) -> Unit)? = null,
    onCreateSpecialityClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SpecialitiesViewModel = viewModel()
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, refreshKey) {
        viewModel.loadSpecialities()
    }

    val isSearching = uiState.searchQuery.isNotBlank()
    val specialityCount = uiState.specialities.size

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading &&
                    uiState.specialities.isEmpty() -> {
                AppLoading(
                    message = "Cargando especialidades..."
                )
            }

            uiState.error != null &&
                    uiState.specialities.isEmpty() -> {
                AppError(
                    message = uiState.error
                        ?: "No se pudieron cargar las especialidades.",
                    onRetry = viewModel::loadSpecialities
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp,
                        bottom = if (
                            onCreateSpecialityClick != null
                        ) {
                            116.dp
                        } else {
                            24.dp
                        }
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SpecialitiesHeader()
                    }

                    item {
                        SpecialitiesSummary(
                            count = specialityCount,
                            isSearching = isSearching
                        )
                    }

                    item {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Buscar especialidad",
                                style = MaterialTheme
                                    .typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            SpecialitySearchBar(
                                query = uiState.searchQuery,
                                onQueryChange =
                                    viewModel::onSearchChange
                            )
                        }
                    }

                    if (
                        uiState.isLoading &&
                        uiState.specialities.isNotEmpty()
                    ) {
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
                                    text = "Actualizando especialidades...",
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!uiState.error.isNullOrBlank()) {
                        item {
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
                                    "Listado de especialidades"
                                },
                                style = MaterialTheme
                                    .typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = specialityCount.toString(),
                                style = MaterialTheme
                                    .typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme
                                    .colorScheme.primary
                            )
                        }
                    }

                    if (uiState.specialities.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = if (isSearching) {
                                    "Sin resultados"
                                } else {
                                    "Sin especialidades"
                                },
                                message = if (isSearching) {
                                    "No se encontraron especialidades que coincidan con la búsqueda."
                                } else {
                                    "Las especialidades registradas aparecerán aquí."
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.specialities,
                            key = { speciality ->
                                speciality.id
                            }
                        ) { speciality ->
                            SpecialityCard(
                                speciality = speciality,
                                onClick = {
                                    onSpecialityClick
                                        ?.invoke(speciality.id)
                                }
                            )
                        }
                    }
                }

                if (onCreateSpecialityClick != null) {
                    ExtendedFloatingActionButton(
                        onClick = onCreateSpecialityClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                        icon = {
                            Icon(
                                imageVector =
                                    Icons.Outlined.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = "Nueva especialidad",
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
}

/**
 * Título y descripción.
 */
@Composable
private fun SpecialitiesHeader() {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Especialidades médicas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Consulta y administra las especialidades disponibles para las atenciones médicas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme
                .colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Resumen del listado.
 *
 * Si hay una búsqueda activa, el contador
 * muestra únicamente los resultados filtrados.
 */
@Composable
private fun SpecialitiesSummary(
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
                        modifier = Modifier.size(27.dp),
                        tint = colors.primary
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
                            "Especialidad registrada"

                        else ->
                            "Especialidades registradas"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimaryContainer
                )
            }
        }
    }
}
