package pe.com.smart.presentation.appointments.form

/*
 * =====================================================
 * ESTADO DEL FORMULARIO DE CITAS
 * =====================================================
 *
 * Aquí guardamos todos los datos que el usuario
 * escribirá al crear o editar una cita médica.
 */
data class AppointmentFormUiState(

    // ID del paciente seleccionado
    val patientId: String = "",

    // ID del médico seleccionado
    val doctorId: String = "",

    // Motivo de la consulta
    val reason: String = "",

    // Fecha de la cita
    val appointmentDate: String = "",

    // Hora de inicio
    val startTime: String = "",

    // Hora de fin
    val endTime: String = "",

    // Estado de la cita
    // Por defecto será PROGRAMADA
    val status: String = "PROGRAMADA",


    /*
     * =================================================
     * MENSAJES DE ERROR DE CADA CAMPO
     * =================================================
     */

    val patientIdError: String? = null,
    val doctorIdError: String? = null,
    val reasonError: String? = null,
    val appointmentDateError: String? = null,
    val startTimeError: String? = null,
    val endTimeError: String? = null,


    /*
     * Indica si el usuario modificó algún dato.
     */
    val hasChanges: Boolean = false,


    /*
     * Indica si estamos cargando una cita
     * cuando se quiere editar.
     */
    val isLoading: Boolean = false,


    /*
     * Indica si estamos guardando la cita.
     */
    val isSaving: Boolean = false,


    /*
     * Guarda un error general si ocurre
     * algún problema al cargar la información.
     */
    val error: String? = null
)