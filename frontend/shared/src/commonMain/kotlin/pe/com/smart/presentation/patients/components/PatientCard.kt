package pe.com.smart.presentation.patients.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.com.smart.domain.model.Patient

@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            /*
             * =================================================
             * CABECERA
             * =================================================
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * Avatar / identificador
                 */
                Surface(
                    modifier =
                        Modifier.size(
                            48.dp
                        ),

                    shape =
                        CircleShape,

                    color =
                        MaterialTheme.colorScheme.primaryContainer
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Badge,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    24.dp
                                ),

                            tint =
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            14.dp
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            2.dp
                        )
                ) {

                    Text(
                        text =
                            patient.fullName,

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow.Ellipsis,

                        color =
                            MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text =
                            "DNI: ${patient.dni}",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /*
             * =================================================
             * CORREO
             * =================================================
             */
            PatientContactRow(
                icon =
                    Icons.Outlined.Email,

                text =
                    patient.email
            )

            /*
             * =================================================
             * TELÉFONO
             * =================================================
             */
            PatientContactRow(
                icon =
                    Icons.Outlined.Phone,

                text =
                    patient.phone
            )
        }
    }
}


/*
 * =====================================================
 * DATO DE CONTACTO
 * =====================================================
 */

@Composable
private fun PatientContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(
                    20.dp
                ),

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                )
        )

        Text(
            text =
                text,

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}