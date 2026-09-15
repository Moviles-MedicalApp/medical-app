package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PatientSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value =
            query,

        onValueChange =
            onQueryChange,

        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp),

        singleLine =
            true,

        shape =
            RoundedCornerShape(16.dp),

        placeholder = {

            Text(
                text =
                    "Buscar por nombre, DNI o correo",

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },

        leadingIcon = {

            Icon(
                imageVector =
                    Icons.Outlined.Search,

                contentDescription =
                    "Buscar paciente",

                tint =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },

        trailingIcon = {

            if (query.isNotBlank()) {

                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Clear,

                        contentDescription =
                            "Limpiar búsqueda",

                        tint =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },

        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor =
                    MaterialTheme.colorScheme.primary,

                unfocusedBorderColor =
                    MaterialTheme.colorScheme.outlineVariant,

                focusedContainerColor =
                    MaterialTheme.colorScheme.surface,

                unfocusedContainerColor =
                    MaterialTheme.colorScheme.surface,

                cursorColor =
                    MaterialTheme.colorScheme.primary
            )
    )
}