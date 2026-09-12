package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PatientSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(
                "Buscar paciente"
            )
        },
        placeholder = {
            Text(
                "Nombre, DNI o correo"
            )
        },
        leadingIcon = {

            Icon(
                imageVector =
                    Icons.Outlined.Search,
                contentDescription = null
            )
        },
        trailingIcon = {

            if (query.isNotEmpty()) {

                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Clear,
                        contentDescription =
                            "Limpiar búsqueda"
                    )
                }
            }
        }
    )
}