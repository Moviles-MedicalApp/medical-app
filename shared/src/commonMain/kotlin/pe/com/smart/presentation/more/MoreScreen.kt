package pe.com.smart.presentation.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Más opciones",
            style =
                MaterialTheme.typography.headlineSmall
        )

        OutlinedButton(
            onClick = {
                // Médicos
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.MedicalServices,
                contentDescription = null
            )

            Text(
                text = "  Médicos"
            )
        }

        OutlinedButton(
            onClick = {
                // Especialidades
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Tag,
                contentDescription = null
            )

            Text(
                text = "  Especialidades"
            )
        }

        OutlinedButton(
            onClick = {
                // Horarios médicos
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Schedule,
                contentDescription = null
            )

            Text(
                text = "  Horarios médicos"
            )
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Logout,
                contentDescription = null
            )

            Text(
                text = "  Cerrar sesión"
            )
        }
    }
}