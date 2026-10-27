package pe.com.smart.presentation.doctors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
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
import pe.com.smart.presentation.doctors.components.DoctorCard
import pe.com.smart.presentation.doctors.components.DoctorSearchBar

@Composable
fun DoctorsScreen(
    refreshKey: Int = 0,
    onDoctorClick: (Int) -> Unit,
    onCreateDoctorClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoctorsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshKey) {
        viewModel.loadDoctors()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.doctors.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error != null && uiState.doctors.isEmpty() -> {
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
                        top = 16.dp,
                        bottom = 132.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DoctorSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchChange
                        )
                    }

                    if (uiState.doctors.isEmpty()) {
                        item {
                            AppEmptyState(
                                title = if (uiState.searchQuery.isBlank()) {
                                    "Sin médicos"
                                } else {
                                    "Sin resultados"
                                },
                                message = if (uiState.searchQuery.isBlank()) {
                                    "No hay médicos registrados."
                                } else {
                                    "No se encontraron médicos que coincidan con la búsqueda."
                                }
                            )
                        }
                    } else {
                        items(
                            items = uiState.doctors,
                            key = { doctor -> doctor.id }
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
                        .padding(end = 16.dp, bottom = 16.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Nuevo médico",
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