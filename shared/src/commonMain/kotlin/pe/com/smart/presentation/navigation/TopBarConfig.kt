package pe.com.smart.presentation.navigation

data class TopBarConfig(
    val title: String = "",
    val visible: Boolean = true,
    val showMenu: Boolean = true,
    val canNavigateBack: Boolean = false,
    val showProfile: Boolean = false
)

fun getTopBarConfig(
    route: String?
): TopBarConfig {

    return when (route) {

        Routes.HOME -> {
            TopBarConfig(
                title = "Medical App",
                showProfile = true
            )
        }

        Routes.APPOINTMENTS -> {
            TopBarConfig(
                title = "Citas"
            )
        }

        Routes.PATIENTS -> {
            TopBarConfig(
                title = "Pacientes"
            )
        }

        Routes.PATIENT_CREATE -> {
            TopBarConfig(
                title = "Nuevo paciente",
                showMenu = false,
                canNavigateBack = true
            )
        }

        Routes.PATIENT_DETAIL -> {
            TopBarConfig(
                title = "Detalle del paciente",
                showMenu = false,
                canNavigateBack = true
            )
        }

        Routes.PATIENT_EDIT -> {
            TopBarConfig(
                title = "Editar paciente",
                showMenu = false,
                canNavigateBack = true
            )
        }

        Routes.DOCTORS -> {
            TopBarConfig(
                title = "Médicos"
            )
        }

        Routes.SPECIALITIES -> {
            TopBarConfig(
                title = "Especialidades"
            )
        }

        Routes.MEDICAL_SCHEDULES -> {
            TopBarConfig(
                title = "Agenda médica"
            )
        }

        Routes.LOGIN,
        Routes.SPLASH -> {

            TopBarConfig(
                visible = false,
                showMenu = false
            )
        }

        else -> {

            TopBarConfig(
                visible = false,
                showMenu = false
            )
        }
    }
}