package pe.com.smart.presentation.schedules

import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule

data class MedicalSchedulesUiState(
    val doctors: List<Doctor> = emptyList(),
    val schedules: List<MedicalSchedule> = emptyList(),
    val searchQuery: String = "",
    val selectedDay: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
