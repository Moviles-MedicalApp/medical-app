package pe.com.smart.presentation.schedules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MedicalSchedulesScreen(
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
                "Agenda médica",

            style =
                MaterialTheme.typography.titleLarge
        )
    }
}