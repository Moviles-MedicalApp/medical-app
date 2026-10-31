package pe.com.smart.presentation.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.com.smart.data.remote.ApiProvider

class MedicalSchedulesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MedicalSchedulesUiState())
    val uiState: StateFlow<MedicalSchedulesUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val doctors = async { ApiProvider.doctorRepository.getDoctors() }
            val schedules = async { ApiProvider.medicalScheduleRepository.getSchedules() }
            val doctorsResult = doctors.await()
            val schedulesResult = schedules.await()
            _uiState.value = MedicalSchedulesUiState(
                doctors = doctorsResult.getOrDefault(emptyList()),
                schedules = schedulesResult.getOrDefault(emptyList()),
                searchQuery = _uiState.value.searchQuery,
                selectedDay = _uiState.value.selectedDay,
                error = doctorsResult.exceptionOrNull()?.message ?: schedulesResult.exceptionOrNull()?.message
            )
        }
    }

    fun onSearchChange(query: String) { _uiState.value = _uiState.value.copy(searchQuery = query) }
    fun onDaySelected(day: String?) { _uiState.value = _uiState.value.copy(selectedDay = day) }
}
