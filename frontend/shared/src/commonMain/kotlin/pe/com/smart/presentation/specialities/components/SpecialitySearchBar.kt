
package pe.com.smart.presentation.specialities.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.com.smart.presentation.components.form.AppSearchField

/**
 * Buscador de especialidades.
 *
 * La búsqueda sigue siendo administrada
 * por SpecialitiesViewModel.
 */
@Composable
fun SpecialitySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = "Buscar por nombre o descripción...",
        contentDescription = "Buscar especialidad",
        modifier = modifier
    )
}
