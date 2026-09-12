package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.com.smart.core.ui.InfoRow
import pe.com.smart.domain.model.Patient

@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    shape = CircleShape,
                    color =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Badge,
                        contentDescription = null,
                        modifier =
                            Modifier.padding(12.dp),
                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                Column {

                    Text(
                        text =
                            patient.fullName,
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "DNI: ${patient.dni}",
                        style =
                            MaterialTheme.typography
                                .bodyMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            InfoRow(
                icon =
                    Icons.Outlined.Email,
                text =
                    patient.email
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            InfoRow(
                icon =
                    Icons.Outlined.Phone,
                text =
                    patient.phone
            )
        }
    }
}