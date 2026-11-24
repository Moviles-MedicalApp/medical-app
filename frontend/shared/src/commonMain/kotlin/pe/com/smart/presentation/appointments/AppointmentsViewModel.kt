
package pe.com.smart.presentation.appointments

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
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.usecase.AppointmentUseCase
import pe.com.smart.domain.usecase.DoctorUseCase
import pe.com.smart.domain.usecase.MedicalScheduleUseCase
import pe.com.smart.domain.usecase.PatientUseCase

private enum class AppointmentUserRole {
    ADMIN,
    DOCTOR,
    PATIENT,
    UNKNOWN
}

class AppointmentsViewModel(
    private val appointmentRepository: AppointmentUseCase,
    private val doctorRepository: DoctorUseCase,
    private val patientRepository: PatientUseCase,
    private val medicalScheduleRepository: MedicalScheduleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        AppointmentsUiState()
    )

    val state: StateFlow<AppointmentsUiState> =
        _state.asStateFlow()

    private var loadJob: Job? = null
    private var schedulesJob: Job? = null
    private var selectedScheduleDoctorId: Int? = null
    private var doctorAgendaStatus: String = "CONFIRMED"

    private val currentRole: AppointmentUserRole
        get() {
            val claims = AccessTokenClaimsDecoder.decode(
                TokenStorage.getToken().orEmpty()
            )
            return when {
                claims?.hasRole("ADMIN") == true ->
                    AppointmentUserRole.ADMIN

                claims?.hasRole("DOCTOR") == true ->
                    AppointmentUserRole.DOCTOR

                claims?.hasRole("PATIENT") == true ->
                    AppointmentUserRole.PATIENT

                else -> AppointmentUserRole.UNKNOWN
            }
        }

    // =====================================================
    // FILTROS
    // =====================================================

    fun onScopeSelected(scope: AppointmentListScope) {
        _state.update {
            it.copy(
                selectedScope = scope,
                selectedStatus = null
            )
        }
    }

    fun onStatusSelected(status: String?) {
        _state.update { current ->
            val allowed = status == null ||
                    status in current.availableStatuses

            if (!allowed) {
                current
            } else {
                current.copy(selectedStatus = status)
            }
        }
    }

    fun onSearchChange(query: String) {
        _state.update {
            it.copy(searchQuery = query)
        }
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                selectedScope = AppointmentListScope.ACTIVE,
                selectedStatus = null,
                searchQuery = ""
            )
        }
    }

    // =====================================================
    // CONSULTA DE CITAS
    // =====================================================

    fun load(
        doctorAgendaStatus: String = this.doctorAgendaStatus
    ) {
        this.doctorAgendaStatus = doctorAgendaStatus
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val role = currentRole

                if (role == AppointmentUserRole.UNKNOWN) {
                    error(
                        "No tienes permisos para consultar las citas."
                    )
                }

                val result = coroutineScope {
                    val appointmentsDeferred = async {
                        when (role) {
                            AppointmentUserRole.PATIENT ->
                                appointmentRepository
                                    .getMyAppointments()
                                    .getOrThrow()

                            AppointmentUserRole.DOCTOR ->
                                appointmentRepository
                                    .getMyDoctorAgenda(
                                        status = doctorAgendaStatus
                                    )
                                    .getOrThrow()

                            AppointmentUserRole.ADMIN ->
                                appointmentRepository
                                    .getAppointments()
                                    .getOrThrow()

                            AppointmentUserRole.UNKNOWN ->
                                emptyList()
                        }
                    }

                    val doctorsDeferred = async {
                        when (role) {
                            AppointmentUserRole.ADMIN,
                            AppointmentUserRole.PATIENT ->
                                doctorRepository
                                    .getDoctors()
                                    .getOrThrow()

                            else -> emptyList()
                        }
                    }

                    val patientsDeferred = async {
                        if (role == AppointmentUserRole.ADMIN) {
                            patientRepository.getPatients()
                                .getOrThrow()
                        } else {
                            emptyList()
                        }
                    }

                    val schedulesDeferred = async {
                        if (role == AppointmentUserRole.ADMIN) {
                            medicalScheduleRepository
                                .getSchedules()
                                .getOrThrow()
                        } else {
                            emptyList()
                        }
                    }

                    AppointmentsUiState(
                        appointments =
                            appointmentsDeferred.await(),
                        doctors = doctorsDeferred.await(),
                        patients = patientsDeferred.await(),
                        schedules = schedulesDeferred.await()
                    )
                }

                _state.update { current ->
                    current.copy(
                        appointments = result.appointments,
                        doctors = result.doctors,
                        patients = result.patients,
                        schedules = if (
                            role == AppointmentUserRole.PATIENT
                        ) {
                            current.schedules
                        } else {
                            result.schedules
                        },
                        isLoading = false,
                        error = null
                    )
                }

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "No se pudieron cargar las citas."
                    )
                }
            }
        }
    }

    // =====================================================
    // HORARIOS DE UN MÉDICO
    // =====================================================

    fun loadSchedulesForDoctor(doctorId: Int) {
        if (
            currentRole != AppointmentUserRole.PATIENT ||
            doctorId <= 0
        ) return

        selectedScheduleDoctorId = doctorId
        schedulesJob?.cancel()

        schedulesJob = viewModelScope.launch {
            _state.update {
                it.copy(schedules = emptyList())
            }

            try {
                val schedules = medicalScheduleRepository
                    .getSchedulesByDoctor(doctorId)
                    .getOrThrow()

                if (selectedScheduleDoctorId == doctorId) {
                    _state.update {
                        it.copy(schedules = schedules)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (selectedScheduleDoctorId == doctorId) {
                    UiMessageManager.error(
                        "No se pudieron cargar los horarios",
                        exception.message
                    )
                }
            }
        }
    }

    // =====================================================
    // CREAR O REPROGRAMAR UNA CITA
    // =====================================================

    fun save(
        existing: Appointment?,
        reason: String,
        date: String,
        start: String,
        end: String,
        patientId: Int,
        doctorId: Int,
        done: () -> Unit
    ) {
        if (_state.value.isSaving) return

        _state.update {
            it.copy(isSaving = true)
        }

        viewModelScope.launch {
            var savedSuccessfully = false

            try {
                require(reason.isNotBlank()) {
                    "Ingresa el motivo de la cita."
                }
                require(date.isNotBlank()) {
                    "Selecciona una fecha."
                }
                require(start.isNotBlank()) {
                    "Selecciona la hora de inicio."
                }
                require(end.isNotBlank()) {
                    "Selecciona la hora de término."
                }
                require(doctorId > 0) {
                    "Selecciona un médico."
                }

                when (currentRole) {
                    AppointmentUserRole.PATIENT -> {
                        check(existing == null) {
                            "Los pacientes no pueden editar citas desde esta pantalla."
                        }

                        appointmentRepository
                            .createMyAppointment(
                                reason = reason.trim(),
                                date = date,
                                start = start,
                                end = end,
                                doctorId = doctorId
                            )
                            .getOrThrow()
                    }

                    AppointmentUserRole.ADMIN -> {
                        require(patientId > 0) {
                            "Selecciona un paciente."
                        }

                        if (existing == null) {
                            appointmentRepository.create(
                                reason = reason.trim(),
                                date = date,
                                start = start,
                                end = end,
                                patientId = patientId,
                                doctorId = doctorId
                            ).getOrThrow()
                        } else {
                            appointmentRepository.update(
                                id = existing.id,
                                reason = reason.trim(),
                                date = date,
                                start = start,
                                end = end,
                                patientId = patientId,
                                doctorId = doctorId
                            ).getOrThrow()
                        }
                    }

                    AppointmentUserRole.DOCTOR ->
                        error(
                            "Los médicos no pueden registrar ni editar citas."
                        )

                    AppointmentUserRole.UNKNOWN ->
                        error(
                            "No tienes permisos para guardar citas."
                        )
                }

                savedSuccessfully = true

                UiMessageManager.success(
                    if (existing == null) {
                        "Cita registrada"
                    } else {
                        "Cita actualizada"
                    }
                )

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                UiMessageManager.error(
                    "No se pudo guardar la cita",
                    exception.message
                )
            } finally {
                _state.update {
                    it.copy(isSaving = false)
                }
            }

            if (savedSuccessfully) {
                done()
                load()
            }
        }
    }

    // =====================================================
    // CAMBIO DE ESTADO
    // =====================================================

    fun changeStatus(
        appointment: Appointment,
        action: String
    ) {
        if (_state.value.isSaving) return

        _state.update {
            it.copy(isSaving = true)
        }

        viewModelScope.launch {
            var updatedSuccessfully = false

            try {
                val isActive = appointment.status in setOf(
                    "SCHEDULED", "CONFIRMED"
                )

                when (action) {
                    "confirm" ->
                        require(appointment.status == "SCHEDULED") {
                            "Solo se pueden confirmar citas programadas."
                        }

                    "complete" ->
                        require(appointment.status == "CONFIRMED") {
                            "Solo se pueden completar citas confirmadas."
                        }

                    "cancel" ->
                        require(isActive) {
                            "Solo se pueden cancelar citas activas."
                        }

                    else ->
                        error("Acción no reconocida: $action")
                }

                when (currentRole) {
                    AppointmentUserRole.PATIENT -> {
                        check(action == "cancel") {
                            "Esta acción no está disponible para pacientes."
                        }

                        appointmentRepository
                            .cancelMyAppointment(appointment.id)
                            .getOrThrow()
                    }

                    AppointmentUserRole.DOCTOR -> {
                        check(action == "complete") {
                            "Esta acción no está disponible para médicos."
                        }

                        appointmentRepository
                            .completeMyDoctorAppointment(
                                appointment.id
                            )
                            .getOrThrow()
                    }

                    AppointmentUserRole.ADMIN -> {
                        when (action) {
                            "confirm" -> appointmentRepository
                                .confirm(appointment.id)
                                .getOrThrow()

                            "complete" -> appointmentRepository
                                .complete(appointment.id)
                                .getOrThrow()

                            "cancel" -> appointmentRepository
                                .cancel(appointment.id)
                                .getOrThrow()
                        }
                    }

                    AppointmentUserRole.UNKNOWN ->
                        error(
                            "No tienes permisos para modificar citas."
                        )
                }

                updatedSuccessfully = true

                UiMessageManager.success(
                    when (action) {
                        "confirm" -> "Cita confirmada"
                        "complete" -> "Cita completada"
                        "cancel" -> "Cita cancelada"
                        else -> "Cita actualizada"
                    }
                )

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                UiMessageManager.error(
                    "No se pudo actualizar la cita",
                    exception.message
                )
            } finally {
                _state.update {
                    it.copy(isSaving = false)
                }
            }

            if (updatedSuccessfully) {
                load()
            }
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        schedulesJob?.cancel()
        super.onCleared()
    }
}
