package pe.com.smart.presentation.patients.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.com.smart.presentation.components.form.AppSearchField

@Composable
fun PatientSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = "Buscar por nombre, DNI o correo",
        contentDescription = "Buscar paciente",
        modifier = modifier
    )
}
