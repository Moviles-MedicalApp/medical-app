package pe.com.smart.presentation.doctors

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DoctorsScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier =
            modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                "Módulo de médicos",

            style =
                MaterialTheme.typography.titleLarge
        )
    }
}