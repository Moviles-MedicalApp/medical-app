package pe.com.smart.presentation.specialities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import pe.com.smart.presentation.specialities.components.SpecialityCard
import pe.com.smart.presentation.specialities.components.SpecialitySearchBar

@Composable
fun SpecialitiesScreen(
    refreshKey: Int = 0,
    onSpecialityClick: ((Int) -> Unit)? = null,
    onCreateSpecialityClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SpecialitiesViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshKey) {
        viewModel.loadSpecialities()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading && uiState.specialities.isEmpty() -> {
                AppLoading(
                    message = "Cargando especialidades..."
                )
            }

            uiState.error != null && uiState.specialities.isEmpty() -> {
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
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 132.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SpecialitySearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchChange
                        )
                    }

                    if (uiState.specialities.isNotEmpty()) {
                        item {
                            SpecialitiesCount(
                                count = uiState.specialities.size,
                                isSearching = uiState.searchQuery.isNotBlank()
                            )
                        }
                    }

                    if (uiState.specialities.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = if (uiState.searchQuery.isBlank()) {
                                    "Sin especialidades"
                                } else {
                                    "Sin resultados"
                                },
                                message = if (uiState.searchQuery.isBlank()) {
                                    "No hay especialidades registradas."
                                } else {
                                    "No se encontraron especialidades que coincidan con la búsqueda."
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.specialities,
                            key = { it.id }
                        ) { speciality ->
                            SpecialityCard(
                                speciality = speciality,
                                onClick = {
                                    onSpecialityClick?.invoke(speciality.id)
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
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = "Nueva especialidad",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialitiesCount(
    count: Int,
    isSearching: Boolean
) {
    Column(
        modifier = Modifier.padding(
            horizontal = 2.dp,
            vertical = 2.dp
        )
    ) {
        Text(
            text = when {
                isSearching && count == 1 -> "1 resultado encontrado"
                isSearching -> "$count resultados encontrados"
                count == 1 -> "1 especialidad registrada"
                else -> "$count especialidades registradas"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
