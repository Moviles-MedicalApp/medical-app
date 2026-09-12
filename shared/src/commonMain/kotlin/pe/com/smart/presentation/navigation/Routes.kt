package pe.com.smart.presentation.navigation

object Routes {

    const val SPLASH =
        "splash"

    const val LOGIN =
        "login"

    const val HOME =
        "home"

    const val APPOINTMENTS =
        "appointments"

    const val PATIENTS =
        "patients"

    const val PATIENT_CREATE =
        "patient_create"

    const val PATIENT_DETAIL =
        "patient_detail/{patientId}"

    const val PATIENT_EDIT =
        "patient_edit/{patientId}"

    const val DOCTORS =
        "doctors"

    const val SPECIALITIES =
        "specialities"

    const val MEDICAL_SCHEDULES =
        "medical_schedules"


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
}