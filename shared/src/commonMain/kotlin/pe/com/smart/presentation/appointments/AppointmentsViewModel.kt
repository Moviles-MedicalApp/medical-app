package pe.com.smart.presentation.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.domain.model.Patient

data class AppointmentsUiState(
    val appointments: List<Appointment> = emptyList(), val doctors: List<Doctor> = emptyList(),
    val patients: List<Patient> = emptyList(), val schedules: List<MedicalSchedule> = emptyList(),
    val isLoading: Boolean = false, val isSaving: Boolean = false, val error: String? = null
)

class AppointmentsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppointmentsUiState())
    val state = _state.asStateFlow()
    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true, error = null)
        val appointments = async { ApiProvider.appointmentRepository.getAppointments() }
        val doctors = async { ApiProvider.doctorRepository.getDoctors() }
        val patients = async { ApiProvider.patientRepository.getPatients() }
        val schedules = async { ApiProvider.medicalScheduleRepository.getSchedules() }
        val a = appointments.await(); val d = doctors.await(); val p = patients.await(); val s = schedules.await()
        _state.value = AppointmentsUiState(a.getOrDefault(emptyList()), d.getOrDefault(emptyList()), p.getOrDefault(emptyList()), s.getOrDefault(emptyList()), error = a.exceptionOrNull()?.message ?: d.exceptionOrNull()?.message ?: p.exceptionOrNull()?.message ?: s.exceptionOrNull()?.message)
    }
    fun save(existing: Appointment?, reason: String, date: String, start: String, end: String, patientId: Int, doctorId: Int, done: () -> Unit) = viewModelScope.launch {
        _state.value = _state.value.copy(isSaving = true)
        val result = if (existing == null) ApiProvider.appointmentRepository.create(reason, date, start, end, patientId, doctorId) else ApiProvider.appointmentRepository.update(existing.id, reason, date, start, end, patientId, doctorId)
        result.onSuccess { UiMessageManager.success(if (existing == null) "Cita registrada" else "Cita actualizada"); done(); load() }
            .onFailure { UiMessageManager.error("No se pudo guardar la cita", it.message) }
        _state.value = _state.value.copy(isSaving = false)
    }
    fun changeStatus(appointment: Appointment, action: String) = viewModelScope.launch {
        _state.value = _state.value.copy(isSaving = true)
        val result = when (action) { "confirm" -> ApiProvider.appointmentRepository.confirm(appointment.id); "complete" -> ApiProvider.appointmentRepository.complete(appointment.id); else -> ApiProvider.appointmentRepository.cancel(appointment.id) }
        result.onSuccess { UiMessageManager.success("Cita ${if (action == "cancel") "cancelada" else if (action == "confirm") "confirmada" else "completada"}"); load() }
            .onFailure { UiMessageManager.error("No se pudo actualizar la cita", it.message) }
        _state.value = _state.value.copy(isSaving = false)
    }
}
