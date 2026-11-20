package pe.com.smart.presentation.doctors.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.com.smart.presentation.components.form.AppSearchField

@Composable
fun DoctorSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = "Buscar médico o especialidad",
        contentDescription = "Buscar médico",
        modifier = modifier
    )
}
