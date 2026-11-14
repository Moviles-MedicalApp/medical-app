
package pe.com.smart.presentation.appointments

import pe.com.smart.domain.model.Appointment
import pe.com.smart.domain.model.Doctor
import pe.com.smart.domain.model.MedicalSchedule
import pe.com.smart.domain.model.Patient

enum class AppointmentListScope {
    ACTIVE,
    HISTORY,
    ALL
}

data class AppointmentsUiState(
    val appointments: List<Appointment> = emptyList(),
    val doctors: List<Doctor> = emptyList(),
    val patients: List<Patient> = emptyList(),
    val schedules: List<MedicalSchedule> = emptyList(),

    val selectedScope: AppointmentListScope =
        AppointmentListScope.ACTIVE,
    val selectedStatus: String? = null,
    val searchQuery: String = "",

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val availableStatuses: List<String>
        get() = when (selectedScope) {
            AppointmentListScope.ACTIVE ->
                listOf("SCHEDULED", "CONFIRMED")

            AppointmentListScope.HISTORY ->
                listOf("COMPLETED", "CANCELLED")

            AppointmentListScope.ALL ->
                listOf(
                    "SCHEDULED",
                    "CONFIRMED",
                    "COMPLETED",
                    "CANCELLED"
                )
        }

    val scopedAppointments: List<Appointment>
        get() = appointments.filter { appointment ->
            selectedScope == AppointmentListScope.ALL ||
                    appointment.status in availableStatuses
        }

    val filteredAppointments: List<Appointment>
        get() {
            val query = searchQuery.trim()

            return scopedAppointments
                .filter { appointment ->
                    val matchesStatus =
                        selectedStatus == null ||
                                appointment.status == selectedStatus

                    val matchesSearch =
                        query.isBlank() ||
                                listOf(
                                    appointment.patientFullName,
                                    appointment.patientDni,
                                    appointment.doctorFullName,
                                    appointment.specialityName,
                                    appointment.reason,
                                    appointment.appointmentDate
                                ).any {
                                    it.contains(
                                        query,
                                        ignoreCase = true
                                    )
                                }

                    matchesStatus && matchesSearch
                }
                .sortedWith(
                    appointmentOperationalComparator
                )
        }
}

internal val appointmentOperationalComparator =
    Comparator<Appointment> { first, second ->
        val firstHistory = first.status in setOf(
            "COMPLETED", "CANCELLED"
        )
        val secondHistory = second.status in setOf(
            "COMPLETED", "CANCELLED"
        )

        when {
            firstHistory != secondHistory ->
                if (firstHistory) 1 else -1

            firstHistory ->
                compareValues(
                    second.appointmentDate,
                    first.appointmentDate
                ).takeIf { it != 0 }
                    ?: compareValues(
                        second.startTime,
                        first.startTime
                    )

            else ->
                compareValues(
                    first.appointmentDate,
                    second.appointmentDate
                ).takeIf { it != 0 }
                    ?: compareValues(
                        first.startTime,
                        second.startTime
                    )
        }
    }
