package pe.com.smart.presentation.navigation

object Routes {

    /*
     * =====================================================
     * AUTH
     * =====================================================
     */

    const val SPLASH =
        "splash"

    const val LOGIN =
        "login"


    /*
     * =====================================================
     * HOME
     * =====================================================
     */

    const val HOME =
        "home"


    /*
     * =====================================================
     * PACIENTES
     * =====================================================
     */

    const val PATIENTS =
        "patients"

    const val PATIENT_CREATE =
        "patient_create"

    const val PATIENT_DETAIL =
        "patient_detail/{patientId}"

    const val PATIENT_EDIT =
        "patient_edit/{patientId}"

    fun patientDetail(
        patientId: Int
    ): String {

        return "patient_detail/$patientId"
    }

    fun patientEdit(
        patientId: Int
    ): String {

        return "patient_edit/$patientId"
    }


    /*
     * =====================================================
     * ESPECIALIDADES
     * =====================================================
     */

    const val SPECIALITIES =
        "specialities"

    const val SPECIALITY_CREATE =
        "speciality_create"

    const val SPECIALITY_DETAIL =
        "speciality_detail/{specialityId}"

    const val SPECIALITY_EDIT =
        "speciality_edit/{specialityId}"

    fun specialityDetail(
        specialityId: Int
    ): String {

        return "speciality_detail/$specialityId"
    }

    fun specialityEdit(
        specialityId: Int
    ): String {

        return "speciality_edit/$specialityId"
    }


    /*
     * =====================================================
     * MÉDICOS
     * =====================================================
     */

    const val DOCTORS =
        "doctors"

    const val DOCTOR_CREATE =
        "doctor_create"

    const val DOCTOR_DETAIL =
        "doctor_detail/{doctorId}"

    const val DOCTOR_EDIT =
        "doctor_edit/{doctorId}"

    fun doctorDetail(
        doctorId: Int
    ): String {

        return "doctor_detail/$doctorId"
    }

    fun doctorEdit(
        doctorId: Int
    ): String {

        return "doctor_edit/$doctorId"
    }


    /*
     * =====================================================
     * CITAS
     * =====================================================
     */

    const val APPOINTMENTS =
        "appointments"

    const val APPOINTMENT_CREATE =
        "appointment_create"

    const val APPOINTMENT_DETAIL =
        "appointment_detail/{appointmentId}"

    const val APPOINTMENT_EDIT =
        "appointment_edit/{appointmentId}"

    fun appointmentDetail(
        appointmentId: Int
    ): String {

        return "appointment_detail/$appointmentId"
    }

    fun appointmentEdit(
        appointmentId: Int
    ): String {

        return "appointment_edit/$appointmentId"
    }


    /*
     * =====================================================
     * AGENDA MÉDICA
     * =====================================================
     */

    const val MEDICAL_SCHEDULES =
        "medical_schedules"

    const val MEDICAL_SCHEDULE_CREATE =
        "medical_schedule_create"

    const val MEDICAL_SCHEDULE_DETAIL =
        "medical_schedule_detail/{scheduleId}"

    const val MEDICAL_SCHEDULE_EDIT =
        "medical_schedule_edit/{scheduleId}"

    fun medicalScheduleDetail(
        scheduleId: Int
    ): String {

        return "medical_schedule_detail/$scheduleId"
    }

    fun medicalScheduleEdit(
        scheduleId: Int
    ): String {

        return "medical_schedule_edit/$scheduleId"
    }
}