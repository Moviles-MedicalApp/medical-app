package pe.com.smart.presentation.appointments.components

// Permite hacer clic sobre la tarjeta
import androidx.compose.foundation.clickable

// Elementos para organizar el contenido
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

// Diseño de la tarjeta
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Compose
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Modelo de una cita médica
import pe.com.smart.domain.model.Appointment


/*
 * =====================================================
 * TARJETA DE UNA CITA MÉDICA
 * =====================================================
 *
 * Esta función muestra los datos principales
 * de una cita dentro de una tarjeta.
 */
@Composable
fun AppointmentCard(
    appointment: Appointment,

    // Acción que ocurrirá cuando toquemos la tarjeta
    onClick: () -> Unit,

    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .fillMaxWidth()

                // Permite abrir el detalle de la cita
                .clickable(
                    onClick = onClick
                ),

        // Bordes redondeados
        shape =
            RoundedCornerShape(
                18.dp
            ),

        // Color de fondo de la tarjeta
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        // Sombra de la tarjeta
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            /*
             * =============================================
             * FECHA Y HORA
             * =============================================
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                // Fecha de la cita
                Text(
                    text =
                        appointment.appointmentDate,

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                // Hora de inicio
                Text(
                    text =
                        appointment.startTime,

                    style =
                        MaterialTheme.typography.titleMedium,

                    color =
                        MaterialTheme.colorScheme.primary
                )
            }


            /*
             * =============================================
             * PACIENTE
             * =============================================
             */
            Text(
                text =
                    "Paciente ID: ${appointment.patientId}",

                style =
                    MaterialTheme.typography.bodyMedium
            )


            /*
             * =============================================
             * MÉDICO
             * =============================================
             */
            Text(
                text =
                    "Médico ID: ${appointment.doctorId}",

                style =
                    MaterialTheme.typography.bodyMedium
            )


            /*
             * =============================================
             * MOTIVO
             * =============================================
             */
            Text(
                text =
                    "Motivo: ${appointment.reason}",

                style =
                    MaterialTheme.typography.bodyMedium
            )


            /*
             * =============================================
             * HORARIO COMPLETO
             * =============================================
             */
            Text(
                text =
                    "Horario: ${appointment.startTime} - ${appointment.endTime}",

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )


            /*
             * =============================================
             * ESTADO
             * =============================================
             */
            Text(
                text =
                    "Estado: ${appointment.status}",

                style =
                    MaterialTheme.typography.bodyMedium,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}