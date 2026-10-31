package pe.com.smart.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        when {

            /*
             * =============================================
             * CARGANDO
             * =============================================
             */
            uiState.isLoading -> {

                CircularProgressIndicator(
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        )
                )
            }

            /*
             * =============================================
             * ERROR
             * =============================================
             */
            uiState.error != null -> {

                HomeError(
                    message =
                        uiState.error
                            ?: "No se pudo cargar la información.",

                    onRetry =
                        viewModel::retry,

                    modifier =
                        Modifier.align(
                            Alignment.Center
                        )
                )
            }

            /*
             * =============================================
             * CONTENIDO
             * =============================================
             */
            else -> {

                HomeContent(
                    uiState =
                        uiState,

                    onAppointmentsClick =
                        onAppointmentsClick,

                    onPatientsClick =
                        onPatientsClick,

                    onDoctorsClick =
                        onDoctorsClick,

                    onSpecialitiesClick =
                        onSpecialitiesClick,

                    onNewAppointmentClick =
                        onNewAppointmentClick,

                    onCalendarClick =
                        onCalendarClick
                )
            }
        }
    }
}


/*
 * =====================================================
 * CONTENIDO PRINCIPAL
 * =====================================================
 */

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeGreeting(uiState.username) }
        if (uiState.unconfirmedAppointments > 0 || uiState.doctorsWithoutSchedules > 0) {
            item {
                OperationalAlerts(
                    unconfirmed = uiState.unconfirmedAppointments,
                    withoutSchedules = uiState.doctorsWithoutSchedules,
                    onAppointments = onAppointmentsClick,
                    onDoctors = onDoctorsClick
                )
            }
        }
        item {
            Column {
                SectionTitle("Acciones rápidas")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactQuickAction("Nueva cita", Icons.Outlined.Add, onNewAppointmentClick, Modifier.weight(1f))
                    CompactQuickAction("Calendario", Icons.Outlined.CalendarMonth, onCalendarClick, Modifier.weight(1f))
                    CompactQuickAction("Paciente", Icons.Outlined.People, onPatientsClick, Modifier.weight(1f))
                }
            }
        }
        item {
            Column {
                SectionTitle("Agenda de hoy")
                Spacer(Modifier.height(14.dp))
                DashboardAgendaCard(uiState.todayAppointments, onAppointmentsClick, onNewAppointmentClick)
            }
        }
        if (uiState.nextAppointment != null) item {
            Column {
                SectionTitle("Próxima cita")
                Spacer(Modifier.height(14.dp))
                NextAppointmentCard(uiState.nextAppointment, onClick = onAppointmentsClick)
            }
        }
        item {
            Column {
                SectionTitle("Resumen operativo")
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Pendientes", uiState.todayAppointments.toString(), Icons.Outlined.CalendarMonth, onAppointmentsClick, Modifier.weight(1f))
                    SummaryCard("Pacientes", uiState.patients.toString(), Icons.Outlined.People, onPatientsClick, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Médicos", uiState.doctors.toString(), Icons.Outlined.MedicalServices, onDoctorsClick, Modifier.weight(1f))
                    SummaryCard("Especialidades", uiState.specialities.toString(), Icons.Outlined.LocalHospital, onSpecialitiesClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OperationalAlerts(
    unconfirmed: Int,
    withoutSchedules: Int,
    onAppointments: () -> Unit,
    onDoctors: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Requiere atención")
        if (unconfirmed > 0) {
            AlertCard(
                "$unconfirmed ${if (unconfirmed == 1) "cita por confirmar" else "citas por confirmar"}",
                "Revisa la agenda activa para confirmar la atención.",
                "Ver citas",
                onAppointments
            )
        }
        if (withoutSchedules > 0) {
            AlertCard(
                "$withoutSchedules ${if (withoutSchedules == 1) "médico sin horario" else "médicos sin horario"}",
                "Configura disponibilidad para habilitar nuevas citas.",
                "Ver médicos",
                onDoctors
            )
        }
    }
}

@Composable
private fun AlertCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CompactQuickAction(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun DashboardAgendaCard(pending: Int, onAgenda: () -> Unit, onNew: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(Modifier.size(48.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) } }
            Spacer(Modifier.height(12.dp))
            Text(if (pending == 0) "Sin citas pendientes" else "$pending citas pendientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(if (pending == 0) "La agenda del día está libre" else "Revisa y gestiona tus próximas atenciones", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            if (pending == 0) Button(onClick = onNew) { Icon(Icons.Outlined.Add, null); Text(" Nueva cita") }
            else Text("Ver agenda", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onAgenda))
        }
    }
}

@Composable
private fun LegacyHomeContent(
    uiState: HomeUiState,
    onAppointmentsClick: () -> Unit,
    onPatientsClick: () -> Unit,
    onDoctorsClick: () -> Unit,
    onSpecialitiesClick: () -> Unit,
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit
) {

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 32.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {

        /*
         * =================================================
         * SALUDO
         * =================================================
         */
        item {

            HomeGreeting(
                username =
                    uiState.username
            )
        }

        /*
         * =================================================
         * RESUMEN
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title =
                        "Resumen"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    SummaryCard(
                        title =
                            "Citas hoy",

                        value =
                            uiState
                                .todayAppointments
                                .toString(),

                        icon =
                            Icons.Outlined.CalendarMonth,

                        modifier =
                            Modifier.weight(1f),

                        onClick =
                            onAppointmentsClick
                    )

                    SummaryCard(
                        title =
                            "Pacientes",

                        value =
                            uiState
                                .patients
                                .toString(),

                        icon =
                            Icons.Outlined.People,

                        modifier =
                            Modifier.weight(1f),

                        onClick =
                            onPatientsClick
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    SummaryCard(
                        title =
                            "Médicos",

                        value =
                            uiState
                                .doctors
                                .toString(),

                        icon =
                            Icons.Outlined.MedicalServices,

                        modifier =
                            Modifier.weight(1f),

                        onClick =
                            onDoctorsClick
                    )

                    SummaryCard(
                        title =
                            "Especialidades",

                        value =
                            uiState
                                .specialities
                                .toString(),

                        icon =
                            Icons.Outlined.LocalHospital,

                        modifier =
                            Modifier.weight(1f),

                        onClick =
                            onSpecialitiesClick
                    )
                }
            }
        }

        /*
         * =================================================
         * AGENDA DE HOY
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title =
                        "Agenda de hoy"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                TodayAgendaCard(
                    appointmentCount =
                        uiState.todayAppointments,

                    onClick =
                        onAppointmentsClick
                )
            }
        }

        /*
         * =================================================
         * PRÓXIMA CITA
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title =
                        "Próxima cita"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                NextAppointmentCard(
                    appointment =
                        uiState.nextAppointment,

                    onClick =
                        onAppointmentsClick
                )
            }
        }

        /*
         * =================================================
         * ACCIONES RÁPIDAS
         * =================================================
         */
        item {

            Column {

                SectionTitle(
                    title =
                        "Acciones rápidas"
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                QuickActions(
                    onNewAppointmentClick =
                        onNewAppointmentClick,

                    onCalendarClick =
                        onCalendarClick
                )
            }
        }
    }
}


/*
 * =====================================================
 * SALUDO
 * =====================================================
 */

@Composable
private fun HomeGreeting(
    username: String
) {

    Column {

        Text(
            text =
                "Buenos días, $username",

            style =
                MaterialTheme.typography.headlineSmall,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Text(
            text =
                "Tu agenda y tareas operativas para hoy",

            style =
                MaterialTheme.typography.bodyLarge,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/*
 * =====================================================
 * TÍTULO DE SECCIÓN
 * =====================================================
 */

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text =
            title,

        style =
            MaterialTheme.typography.headlineSmall,

        fontWeight =
            FontWeight.Bold,

        color =
            MaterialTheme.colorScheme.onBackground
    )
}


/*
 * =====================================================
 * TARJETA DE RESUMEN
 * =====================================================
 */

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .height(96.dp)
                .clickable(
                    onClick = onClick
                ),

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
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),

            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        title,

                    style =
                        MaterialTheme.typography.titleSmall,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    modifier =
                        Modifier.weight(1f)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(27.dp),

                    tint =
                        MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text =
                    value,

                style =
                    MaterialTheme.typography.headlineMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


/*
 * =====================================================
 * AGENDA DE HOY
 * =====================================================
 */

@Composable
private fun TodayAgendaCard(
    appointmentCount: Int,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(20.dp),

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
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 24.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(
                modifier =
                    Modifier.size(52.dp),

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
                            Icons.Outlined.CalendarMonth,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(28.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            if (appointmentCount == 0) {

                Text(
                    text =
                        "Sin citas para hoy",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "La agenda del día está libre",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                Text(
                    text =
                        "$appointmentCount ${
                            if (appointmentCount == 1) {
                                "cita programada"
                            } else {
                                "citas programadas"
                            }
                        }",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Revisa la agenda del día",

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


/*
 * =====================================================
 * PRÓXIMA CITA
 * =====================================================
 */

@Composable
private fun NextAppointmentCard(
    appointment: AppointmentSummary?,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(20.dp),

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

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier =
                    Modifier.size(52.dp),

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
                            Icons.Outlined.EventAvailable,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(27.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                if (appointment == null) {

                    Text(
                        text =
                            "No hay próximas citas",

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "No hay atenciones pendientes registradas.",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {

                    Text(
                        text =
                            appointment.time,

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            appointment.patientName,

                        style =
                            MaterialTheme.typography.bodyLarge,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(
                        text =
                            "${appointment.speciality} · ${appointment.doctorName}",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/*
 * =====================================================
 * ACCIONES RÁPIDAS
 * =====================================================
 */

@Composable
private fun QuickActions(
    onNewAppointmentClick: () -> Unit,
    onCalendarClick: () -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        QuickActionCard(
            title =
                "Nueva cita",

            subtitle =
                "Registrar atención",

            icon =
                Icons.Outlined.Add,

            onClick =
                onNewAppointmentClick,

            modifier =
                Modifier.weight(1f)
        )

        QuickActionCard(
            title =
                "Agenda",

            subtitle =
                "Ver horarios",

            icon =
                Icons.Outlined.Schedule,

            onClick =
                onCalendarClick,

            modifier =
                Modifier.weight(1f)
        )
    }
}


/*
 * =====================================================
 * ACCIÓN RÁPIDA
 * =====================================================
 */

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier.clickable(
                onClick = onClick
            ),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        border =
            BorderStroke(
                width = 1.dp,

                color =
                    MaterialTheme.colorScheme.outlineVariant
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {

            Surface(
                modifier =
                    Modifier.size(42.dp),

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
                            icon,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(22.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    title,

                style =
                    MaterialTheme.typography.titleSmall,

                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text =
                    subtitle,

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/*
 * =====================================================
 * ERROR
 * =====================================================
 */

@Composable
private fun HomeError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier =
            modifier.padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text =
                "No se pudo cargar el resumen",

            style =
                MaterialTheme.typography.titleMedium,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                message,

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "Reintentar",

            color =
                MaterialTheme.colorScheme.primary,

            fontWeight =
                FontWeight.SemiBold,

            modifier =
                Modifier.clickable(
                    onClick = onRetry
                )
        )
    }
}
