package pe.com.smart.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.usecase.AppointmentUseCase
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.domain.usecase.MedicalScheduleUseCase
import pe.com.smart.domain.usecase.PatientUseCase

/**
 * Dashboard operativo de MedicalApp.
 *
 * Las estadísticas se cargan por rol.
 * La identidad del usuario se administra
 * independientemente mediante
 * SessionIdentityStore.
 */
class HomeViewModel(
    private val appointmentRepository: AppointmentUseCase,
    private val patientRepository: PatientUseCase,
    private val doctorRepository: DoctorUseCase,
    private val medicalScheduleRepository: MedicalScheduleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState()
    )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private var dashboardJob: Job? = null

    companion object {
        private const val LOAD_TIMEOUT_MS = 15_000L
    }

    init {
        loadDashboard()
    }

    /**
     * Carga las estadísticas sin consultar
     * los datos personales del usuario.
     *
     * Si una solicitud no termina, muestra
     * un error en lugar de mantener una
     * pantalla de carga indefinidamente.
     */
    fun loadDashboard() {
        dashboardJob?.cancel()

        dashboardJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                withTimeout(LOAD_TIMEOUT_MS) {
                    val claims =
                        AccessTokenClaimsDecoder.decode(
                            TokenStorage.getToken()
                                .orEmpty()
                        )

                    val role = when {
                        claims?.hasRole("ADMIN") == true ->
                            HomeRole.ADMIN

                        claims?.hasRole("DOCTOR") == true ->
                            HomeRole.DOCTOR

                        claims?.hasRole("PATIENT") == true ->
                            HomeRole.PATIENT

                        else ->
                            HomeRole.UNKNOWN
                    }

                    if (role == HomeRole.UNKNOWN) {
                        throw IllegalStateException(
                            "No tienes permisos para acceder al panel."
                        )
                    }

                    val username = claims
                        ?.displayFirstName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Usuario"

                    val milliseconds =
                        kotlin.time.Clock.System.now()
                            .toEpochMilliseconds()

                    val now =
                        Instant.fromEpochMilliseconds(
                            milliseconds
                        ).toLocalDateTime(
                            TimeZone.currentSystemDefault()
                        )

                    val today = now.date.toString()

                    val currentTime =
                        "${now.hour.toString().padStart(2, '0')}:" +
                                now.minute.toString()
                                    .padStart(2, '0')

                    when (role) {
                        HomeRole.DOCTOR -> {
                            loadDoctorDashboard(
                                username = username,
                                today = today,
                                currentTime = currentTime
                            )
                        }

                        HomeRole.PATIENT -> {
                            loadPatientDashboard(
                                username = username,
                                today = today,
                                currentTime = currentTime
                            )
                        }

                        HomeRole.ADMIN -> {
                            loadAdminDashboard(
                                username = username,
                                today = today,
                                currentTime = currentTime
                            )
                        }

                        HomeRole.UNKNOWN -> Unit
                    }
                }

            } catch (
                exception: TimeoutCancellationException
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "El servidor está tardando demasiado en responder. Comprueba la conexión e inténtalo nuevamente."
                    )
                }

            } catch (
                exception: CancellationException
            ) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "No se pudo cargar el resumen."
                    )
                }
            }
        }
    }

    // =====================================================
    // DASHBOARD MÉDICO
    // =====================================================

    private suspend fun loadDoctorDashboard(
        username: String,
        today: String,
        currentTime: String
    ) {
        val appointments =
            appointmentRepository
                .getMyDoctorAgenda(
                    status = "CONFIRMED"
                )
                .getOrThrow()

        val confirmed = appointments
            .filter {
                it.status == "CONFIRMED"
            }
            .sortedWith(appointmentComparator)

        val upcoming = confirmed
            .filter {
                it.isUpcoming(today, currentTime)
            }

        val todayCount = confirmed.count {
            it.appointmentDate == today
        }

        _uiState.value = HomeUiState(
            username = username,
            role = HomeRole.DOCTOR,
            todayAppointments = todayCount,
            upcomingAppointments = upcoming.size,
            activeAppointments = confirmed.size,
            nextAppointment = upcoming
                .firstOrNull()
                ?.toSummary(),
            isLoading = false,
            error = null
        )
    }

    // =====================================================
    // DASHBOARD PACIENTE
    // =====================================================

    private suspend fun loadPatientDashboard(
        username: String,
        today: String,
        currentTime: String
    ) {
        val appointments =
            appointmentRepository
                .getMyAppointments()
                .getOrThrow()

        val active = appointments
            .filter {
                it.status == "SCHEDULED" ||
                        it.status == "CONFIRMED"
            }
            .sortedWith(appointmentComparator)

        val upcoming = active.filter {
            it.isUpcoming(today, currentTime)
        }

        val todayCount = active.count {
            it.appointmentDate == today
        }

        _uiState.value = HomeUiState(
            username = username,
            role = HomeRole.PATIENT,
            todayAppointments = todayCount,
            upcomingAppointments = upcoming.size,
            activeAppointments = active.size,
            nextAppointment = upcoming
                .firstOrNull()
                ?.toSummary(),
            isLoading = false,
            error = null
        )
    }

    // =====================================================
    // DASHBOARD ADMINISTRADOR
    // =====================================================

    private suspend fun loadAdminDashboard(
        username: String,
        today: String,
        currentTime: String
    ) = coroutineScope {

        val patientsDeferred = async {
            patientRepository.getPatients()
        }

        val doctorsDeferred = async {
            doctorRepository.getDoctors()
        }

        val appointmentsDeferred = async {
            appointmentRepository.getAppointments()
        }

        val schedulesDeferred = async {
            medicalScheduleRepository.getSchedules()
        }

        val patients = patientsDeferred
            .await()
            .getOrThrow()

        val doctors = doctorsDeferred
            .await()
            .getOrThrow()

        val appointments = appointmentsDeferred
            .await()
            .getOrThrow()

        val schedules = schedulesDeferred
            .await()
            .getOrThrow()

        val active = appointments
            .filter {
                it.status == "SCHEDULED" ||
                        it.status == "CONFIRMED"
            }
            .sortedWith(appointmentComparator)

        val upcoming = active.filter {
            it.isUpcoming(today, currentTime)
        }

        val todayCount = active.count {
            it.appointmentDate == today
        }

        val unconfirmedCount = appointments.count {
            it.status == "SCHEDULED"
        }

        val doctorsWithoutSchedules =
            doctors.count { doctor ->
                schedules.none {
                    it.doctorId == doctor.id
                }
            }

        val specialitiesCount = doctors
            .map { it.specialityId }
            .distinct()
            .size

        _uiState.value = HomeUiState(
            username = username,
            role = HomeRole.ADMIN,
            todayAppointments = todayCount,
            upcomingAppointments = upcoming.size,
            activeAppointments = active.size,
            patients = patients.size,
            doctors = doctors.size,
            specialities = specialitiesCount,
            unconfirmedAppointments = unconfirmedCount,
            doctorsWithoutSchedules =
                doctorsWithoutSchedules,
            nextAppointment = upcoming
                .firstOrNull()
                ?.toSummary(),
            isLoading = false,
            error = null
        )
    }

    fun retry() {
        loadDashboard()
    }

    override fun onCleared() {
        dashboardJob?.cancel()
        super.onCleared()
    }
}

private val appointmentComparator =
    compareBy<Appointment> {
        it.appointmentDate
    }.thenBy {
        it.startTime
    }

private fun Appointment.isUpcoming(
    today: String,
    currentTime: String
): Boolean {
    return when {
        appointmentDate > today -> true
        appointmentDate < today -> false
        else -> startTime.take(5) >= currentTime
    }
}

private fun Appointment.toSummary(): AppointmentSummary {
    return AppointmentSummary(
        id = id,
        time = "$appointmentDate · $timeRange",
        patientName = patientFullName,
        speciality = specialityName,
        doctorName = doctorFullName,
        status = status
    )
}
