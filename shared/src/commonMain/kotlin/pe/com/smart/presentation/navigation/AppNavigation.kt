package pe.com.smart.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import pe.com.smart.core.ui.AppDrawer
import pe.com.smart.core.ui.AppMessageDialog
import pe.com.smart.core.ui.AppScreenScaffold
import pe.com.smart.core.ui.AppSnackbar
import pe.com.smart.core.ui.message.UiMessage
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.local.SessionManager
import pe.com.smart.data.local.TokenStorage

// =====================================================
// CITAS
// =====================================================

// Pantalla que muestra la lista de citas
import pe.com.smart.presentation.appointments.AppointmentsScreen

// Pantalla para registrar o editar una cita
import pe.com.smart.presentation.appointments.form.AppointmentFormScreen

// =====================================================
// DEMÁS PANTALLAS
// =====================================================

import pe.com.smart.presentation.doctors.DoctorsScreen
import pe.com.smart.presentation.home.HomeScreen
import pe.com.smart.presentation.login.LoginScreen
import pe.com.smart.presentation.patients.PatientsScreen
import pe.com.smart.presentation.patients.detail.PatientDetailScreen
import pe.com.smart.presentation.patients.form.PatientFormScreen
import pe.com.smart.presentation.schedules.MedicalSchedulesScreen
import pe.com.smart.presentation.specialities.SpecialitiesScreen
import pe.com.smart.presentation.splash.SplashScreen


/*
 * =====================================================
 * NAVEGACIÓN PRINCIPAL DE LA APLICACIÓN
 * =====================================================
 */
@Composable
fun AppNavigation() {

    // Controlador de navegación
    val navController =
        rememberNavController()

    // Estado del menú lateral
    val drawerState =
        rememberDrawerState(
            initialValue = DrawerValue.Closed
        )

    val scope =
        rememberCoroutineScope()


    /*
     * ==================================================
     * NOTIFICACIÓN GLOBAL
     * ==================================================
     */

    var currentUiMessage by
    remember {
        mutableStateOf<UiMessage?>(null)
    }

    var showUiMessage by
    remember {
        mutableStateOf(false)
    }


    /*
     * ==================================================
     * SESIÓN EXPIRADA
     * ==================================================
     */

    var showSessionExpiredDialog by
    remember {
        mutableStateOf(false)
    }

    var handlingSessionExpiration by
    remember {
        mutableStateOf(false)
    }


    /*
     * ==================================================
     * REFRESCO DE PACIENTES
     * ==================================================
     */

    var patientsRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    var patientDetailRefreshKey by
    remember {
        mutableIntStateOf(0)
    }


    /*
     * ==================================================
     * REFRESCO DE CITAS
     * ==================================================
     *
     * Cada vez que registramos una cita,
     * este número aumenta.
     *
     * AppointmentsScreen detectará el cambio
     * y volverá a cargar las citas.
     */

    var appointmentsRefreshKey by
    remember {
        mutableIntStateOf(0)
    }


    /*
     * ==================================================
     * RUTA ACTUAL
     * ==================================================
     */

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry
            ?.destination
            ?.route


    /*
     * Pantallas que pueden mostrar
     * el menú lateral.
     */
    val drawerRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.PATIENTS,
            Routes.DOCTORS,
            Routes.SPECIALITIES,
            Routes.MEDICAL_SCHEDULES
        )


    /*
     * Pantallas que muestran
     * barra inferior.
     */
    val bottomBarRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.PATIENTS
        )


    /*
     * ==================================================
     * RUTAS QUE REQUIEREN INICIAR SESIÓN
     * ==================================================
     */

    val authenticatedRoutes =
        setOf(

            Routes.HOME,

            // CITAS
            Routes.APPOINTMENTS,
            Routes.APPOINTMENT_CREATE,

            // PACIENTES
            Routes.PATIENTS,
            Routes.PATIENT_CREATE,
            Routes.PATIENT_DETAIL,
            Routes.PATIENT_EDIT,

            // OTROS MÓDULOS
            Routes.DOCTORS,
            Routes.SPECIALITIES,
            Routes.MEDICAL_SCHEDULES
        )


    val showDrawer =
        currentRoute in drawerRoutes

    val showBottomBar =
        currentRoute in bottomBarRoutes

    val isAuthenticatedRoute =
        currentRoute in authenticatedRoutes


    /*
     * ==================================================
     * ESCUCHA GLOBAL DE NOTIFICACIONES
     * ==================================================
     */

    LaunchedEffect(Unit) {

        UiMessageManager
            .messages
            .collectLatest { message ->

                // Ocultamos la notificación anterior
                showUiMessage =
                    false

                delay(120)

                // Guardamos el nuevo mensaje
                currentUiMessage =
                    message

                // Mostramos el mensaje
                showUiMessage =
                    true


                // Tiempo de duración
                val duration =
                    when (message) {

                        is UiMessage.Error ->
                            4500L

                        is UiMessage.Warning ->
                            4000L

                        is UiMessage.Success ->
                            3000L

                        is UiMessage.Info ->
                            3000L
                    }


                delay(duration)

                showUiMessage =
                    false

                delay(250)

                currentUiMessage =
                    null
            }
    }


    /*
     * ==================================================
     * CONTROL GLOBAL DE SESIÓN EXPIRADA
     * ==================================================
     */

    LaunchedEffect(Unit) {

        SessionManager
            .sessionExpired
            .collect {

                // Evita procesar varios errores 401 juntos
                if (handlingSessionExpiration) {
                    return@collect
                }


                // Si ya estamos en Login,
                // no hacemos nada
                if (
                    navController
                        .currentDestination
                        ?.route == Routes.LOGIN
                ) {
                    return@collect
                }


                handlingSessionExpiration =
                    true


                // Cerramos el menú lateral
                if (drawerState.isOpen) {

                    drawerState.snapTo(
                        DrawerValue.Closed
                    )
                }


                // Eliminamos el token
                TokenStorage.clearToken()


                // Ocultamos notificaciones
                showUiMessage =
                    false

                currentUiMessage =
                    null


                // Volvemos al Login
                navController.navigate(
                    Routes.LOGIN
                ) {

                    popUpTo(
                        Routes.HOME
                    ) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }


                // Mostramos aviso
                showSessionExpiredDialog =
                    true
            }
    }


    /*
     * ==================================================
     * CONTENEDOR PRINCIPAL
     * ==================================================
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {


        /*
         * ==================================================
         * SPLASH / LOGIN
         * ==================================================
         */

        if (!isAuthenticatedRoute) {

            AppNavHost(

                navController =
                    navController,

                patientsRefreshKey =
                    patientsRefreshKey,

                patientDetailRefreshKey =
                    patientDetailRefreshKey,

                appointmentsRefreshKey =
                    appointmentsRefreshKey,

                onPatientsChanged = {

                    patientsRefreshKey++
                },

                onPatientDetailChanged = {

                    patientDetailRefreshKey++
                },

                onAppointmentsChanged = {

                    appointmentsRefreshKey++
                },

                onOpenDrawer = {}
            )

        } else {


            /*
             * ==================================================
             * ZONA AUTENTICADA
             * ==================================================
             */

            ModalNavigationDrawer(

                drawerState =
                    drawerState,

                gesturesEnabled =
                    showDrawer,

                drawerContent = {

                    AppDrawer(

                        currentRoute =
                            currentRoute,

                        username =
                            "admin",

                        onNavigate = { route ->

                            scope.launch {

                                // Cerramos primero el menú
                                drawerState.close()

                                // Navegamos
                                navigateMainRoute(
                                    navController =
                                        navController,

                                    currentRoute =
                                        currentRoute,

                                    route =
                                        route
                                )
                            }
                        },

                        onLogout = {

                            scope.launch {

                                showSessionExpiredDialog =
                                    false

                                handlingSessionExpiration =
                                    false

                                showUiMessage =
                                    false

                                currentUiMessage =
                                    null

                                drawerState.close()

                                TokenStorage.clearToken()

                                navController.navigate(
                                    Routes.LOGIN
                                ) {

                                    popUpTo(
                                        Routes.HOME
                                    ) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            ) {


                AppNavHost(

                    navController =
                        navController,

                    patientsRefreshKey =
                        patientsRefreshKey,

                    patientDetailRefreshKey =
                        patientDetailRefreshKey,

                    appointmentsRefreshKey =
                        appointmentsRefreshKey,

                    onPatientsChanged = {

                        patientsRefreshKey++
                    },

                    onPatientDetailChanged = {

                        patientDetailRefreshKey++
                    },

                    onAppointmentsChanged = {

                        appointmentsRefreshKey++
                    },

                    onOpenDrawer = {

                        if (showDrawer) {

                            scope.launch {

                                drawerState.open()
                            }
                        }
                    }
                )
            }
        }


        /*
         * ==================================================
         * NOTIFICACIÓN GLOBAL
         * ==================================================
         */

        AppSnackbar(

            message =
                currentUiMessage,

            visible =
                showUiMessage,

            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,

                        bottom =
                            if (showBottomBar) {
                                92.dp
                            } else {
                                20.dp
                            }
                    )
        )
    }


    /*
     * ==================================================
     * MODAL DE SESIÓN EXPIRADA
     * ==================================================
     */

    AppMessageDialog(

        visible =
            showSessionExpiredDialog,

        title =
            "Sesión expirada",

        message =
            "Tu sesión ha expirado. Por seguridad, inicia sesión nuevamente.",

        confirmText =
            "Entendido",

        onDismiss = {

            showSessionExpiredDialog =
                false

            handlingSessionExpiration =
                false
        }
    )
}


/*
 * =====================================================
 * NAV HOST
 * =====================================================
 *
 * Aquí declaramos todas las pantallas
 * de la aplicación.
 */
@Composable
private fun AppNavHost(

    navController: NavHostController,

    // Pacientes
    patientsRefreshKey: Int,
    patientDetailRefreshKey: Int,

    // Citas
    appointmentsRefreshKey: Int,

    // Acciones pacientes
    onPatientsChanged: () -> Unit,
    onPatientDetailChanged: () -> Unit,

    // Acción citas
    onAppointmentsChanged: () -> Unit,

    onOpenDrawer: () -> Unit,

    modifier: Modifier = Modifier
) {

    NavHost(

        navController =
            navController,

        startDestination =
            Routes.SPLASH,

        modifier =
            modifier,

        enterTransition = {
            noEnterTransition()
        },

        exitTransition = {
            noExitTransition()
        },

        popEnterTransition = {
            noEnterTransition()
        },

        popExitTransition = {
            noExitTransition()
        }
    ) {


        /*
         * =================================================
         * SPLASH
         * =================================================
         */

        composable(
            Routes.SPLASH
        ) {

            SplashScreen(

                onFinished = {

                    val destination =
                        if (
                            TokenStorage.hasToken()
                        ) {

                            Routes.HOME

                        } else {

                            Routes.LOGIN
                        }


                    navController.navigate(
                        destination
                    ) {

                        popUpTo(
                            Routes.SPLASH
                        ) {
                            inclusive = true
                        }

                        launchSingleTop =
                            true
                    }
                }
            )
        }


        /*
         * =================================================
         * LOGIN
         * =================================================
         */

        composable(
            Routes.LOGIN
        ) {

            LoginScreen(

                onLoginSuccess = {

                    navController.navigate(
                        Routes.HOME
                    ) {

                        popUpTo(
                            Routes.LOGIN
                        ) {
                            inclusive = true
                        }

                        launchSingleTop =
                            true
                    }
                }
            )
        }


        /*
         * =================================================
         * HOME
         * =================================================
         */

        composable(
            route =
                Routes.HOME
        ) {

            AppScreenScaffold(

                title =
                    "Medical App",

                currentRoute =
                    Routes.HOME,

                showMenu =
                    true,

                showProfile =
                    true,

                showBottomBar =
                    true,

                onMenuClick =
                    onOpenDrawer,

                onProfileClick = {

                    // Perfil posteriormente
                },

                onNavigate = { route ->

                    navigateMainRoute(
                        navController =
                            navController,

                        currentRoute =
                            Routes.HOME,

                        route =
                            route
                    )
                }
            ) { innerPadding ->


                HomeScreen(

                    /*
                     * Abrir listado de citas
                     */
                    onAppointmentsClick = {

                        navigateMainRoute(
                            navController =
                                navController,

                            currentRoute =
                                Routes.HOME,

                            route =
                                Routes.APPOINTMENTS
                        )
                    },


                    /*
                     * Abrir pacientes
                     */
                    onPatientsClick = {

                        navigateMainRoute(
                            navController =
                                navController,

                            currentRoute =
                                Routes.HOME,

                            route =
                                Routes.PATIENTS
                        )
                    },


                    /*
                     * Abrir médicos
                     */
                    onDoctorsClick = {

                        navigateMainRoute(
                            navController =
                                navController,

                            currentRoute =
                                Routes.HOME,

                            route =
                                Routes.DOCTORS
                        )
                    },


                    /*
                     * Abrir especialidades
                     */
                    onSpecialitiesClick = {

                        navigateMainRoute(
                            navController =
                                navController,

                            currentRoute =
                                Routes.HOME,

                            route =
                                Routes.SPECIALITIES
                        )
                    },


                    /*
                     * =================================================
                     * NUEVA CITA DESDE INICIO
                     * =================================================
                     */
                    onNewAppointmentClick = {

                        navController.navigate(
                            Routes.APPOINTMENT_CREATE
                        )
                    },


                    /*
                     * Agenda médica
                     */
                    onCalendarClick = {

                        navigateMainRoute(
                            navController =
                                navController,

                            currentRoute =
                                Routes.HOME,

                            route =
                                Routes.MEDICAL_SCHEDULES
                        )
                    },


                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * CITAS
         * =================================================
         *
         * Pantalla que muestra todas las citas.
         */

        composable(
            route =
                Routes.APPOINTMENTS
        ) {

            AppScreenScaffold(

                title =
                    "Citas",

                currentRoute =
                    Routes.APPOINTMENTS,

                showMenu =
                    true,

                showBottomBar =
                    true,

                onMenuClick =
                    onOpenDrawer,

                onNavigate = { route ->

                    navigateMainRoute(
                        navController =
                            navController,

                        currentRoute =
                            Routes.APPOINTMENTS,

                        route =
                            route
                    )
                }
            ) { innerPadding ->


                AppointmentsScreen(

                    /*
                     * Actualiza la lista cuando
                     * se registra una nueva cita.
                     */
                    refreshKey =
                        appointmentsRefreshKey,


                    /*
                     * Cuando tocamos una cita.
                     *
                     * Más adelante aquí conectaremos:
                     * Detalle / Editar.
                     */
                    onAppointmentClick = { appointmentId ->

                        // Pendiente: detalle de cita
                    },


                    /*
                     * =================================================
                     * BOTÓN + NUEVA CITA
                     * =================================================
                     *
                     * Este era el botón que no hacía nada.
                     *
                     * Ahora abre el formulario.
                     */
                    onCreateAppointmentClick = {

                        navController.navigate(
                            Routes.APPOINTMENT_CREATE
                        )
                    },


                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * NUEVA CITA
         * =================================================
         *
         * Pantalla para registrar una cita médica.
         */

        composable(

            route =
                Routes.APPOINTMENT_CREATE,

            enterTransition = {

                slideInFromRight()
            },

            exitTransition = {

                noExitTransition()
            },

            popEnterTransition = {

                noEnterTransition()
            },

            popExitTransition = {

                slideOutToRight()
            }
        ) {


            /*
             * Cada vez que pulsamos Atrás,
             * este número aumenta.
             *
             * AppointmentFormScreen lo detecta.
             */
            var backRequestKey by
            remember {
                mutableIntStateOf(0)
            }


            AppScreenScaffold(

                title =
                    "Nueva cita",

                currentRoute =
                    Routes.APPOINTMENT_CREATE,

                // Flecha para regresar
                showBack =
                    true,

                onBackClick = {

                    /*
                     * No regresamos directamente.
                     *
                     * Primero el formulario revisa
                     * si existen cambios sin guardar.
                     */
                    backRequestKey++
                }
            ) { innerPadding ->


                AppointmentFormScreen(

                    /*
                     * null significa:
                     * NUEVA CITA.
                     *
                     * Cuando editemos una cita
                     * aquí se enviará su ID.
                     */
                    appointmentId =
                        null,


                    /*
                     * Detecta cuando pulsamos Atrás.
                     */
                    backRequestKey =
                        backRequestKey,


                    /*
                     * Si el usuario confirma regresar.
                     */
                    onBackConfirmed = {

                        navController.popBackStack()
                    },


                    /*
                     * =================================================
                     * CITA GUARDADA
                     * =================================================
                     *
                     * 1. Actualizamos la lista.
                     * 2. Regresamos a Citas.
                     */
                    onSaved = {

                        onAppointmentsChanged()

                        navController.popBackStack()
                    },


                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * PACIENTES
         * =================================================
         */

        composable(
            route =
                Routes.PATIENTS
        ) {

            AppScreenScaffold(

                title =
                    "Pacientes",

                currentRoute =
                    Routes.PATIENTS,

                showMenu =
                    true,

                showBottomBar =
                    true,

                onMenuClick =
                    onOpenDrawer,

                onNavigate = { route ->

                    navigateMainRoute(
                        navController =
                            navController,

                        currentRoute =
                            Routes.PATIENTS,

                        route =
                            route
                    )
                }
            ) { innerPadding ->


                PatientsScreen(

                    refreshKey =
                        patientsRefreshKey,

                    onPatientClick = { patientId ->

                        navController.navigate(
                            Routes.patientDetail(
                                patientId
                            )
                        )
                    },

                    onCreatePatientClick = {

                        navController.navigate(
                            Routes.PATIENT_CREATE
                        )
                    },

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * NUEVO PACIENTE
         * =================================================
         */

        composable(

            route =
                Routes.PATIENT_CREATE,

            enterTransition = {

                slideInFromRight()
            },

            exitTransition = {

                noExitTransition()
            },

            popEnterTransition = {

                noEnterTransition()
            },

            popExitTransition = {

                slideOutToRight()
            }
        ) {


            var backRequestKey by
            remember {
                mutableIntStateOf(0)
            }


            AppScreenScaffold(

                title =
                    "Nuevo paciente",

                currentRoute =
                    Routes.PATIENT_CREATE,

                showBack =
                    true,

                onBackClick = {

                    backRequestKey++
                }
            ) { innerPadding ->


                PatientFormScreen(

                    patientId =
                        null,

                    backRequestKey =
                        backRequestKey,

                    onBackConfirmed = {

                        navController.popBackStack()
                    },

                    onSaved = {

                        onPatientsChanged()

                        navController.popBackStack()
                    },

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * DETALLE DEL PACIENTE
         * =================================================
         */

        composable(

            route =
                Routes.PATIENT_DETAIL,

            arguments =
                listOf(

                    navArgument(
                        "patientId"
                    ) {

                        type =
                            NavType.IntType
                    }
                ),

            enterTransition = {

                slideInFromRight()
            },

            exitTransition = {

                noExitTransition()
            },

            popEnterTransition = {

                noEnterTransition()
            },

            popExitTransition = {

                slideOutToRight()
            }
        ) { backStackEntry ->


            val patientId =
                backStackEntry
                    .arguments
                    ?.read {

                        getIntOrNull(
                            "patientId"
                        )
                    }
                    ?: return@composable


            AppScreenScaffold(

                title =
                    "Detalle del paciente",

                currentRoute =
                    Routes.PATIENT_DETAIL,

                showBack =
                    true,

                onBackClick = {

                    navController.popBackStack()
                }
            ) { innerPadding ->


                PatientDetailScreen(

                    patientId =
                        patientId,

                    refreshKey =
                        patientDetailRefreshKey,

                    onEditClick = { id ->

                        navController.navigate(
                            Routes.patientEdit(
                                id
                            )
                        )
                    },

                    onDeleted = {

                        onPatientsChanged()

                        navController.popBackStack(

                            route =
                                Routes.PATIENTS,

                            inclusive =
                                false
                        )
                    },

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * EDITAR PACIENTE
         * =================================================
         */

        composable(

            route =
                Routes.PATIENT_EDIT,

            arguments =
                listOf(

                    navArgument(
                        "patientId"
                    ) {

                        type =
                            NavType.IntType
                    }
                ),

            enterTransition = {

                slideInFromRight()
            },

            exitTransition = {

                noExitTransition()
            },

            popEnterTransition = {

                noEnterTransition()
            },

            popExitTransition = {

                slideOutToRight()
            }
        ) { backStackEntry ->


            val patientId =
                backStackEntry
                    .arguments
                    ?.read {

                        getIntOrNull(
                            "patientId"
                        )
                    }
                    ?: return@composable


            var backRequestKey by
            remember {
                mutableIntStateOf(0)
            }


            AppScreenScaffold(

                title =
                    "Editar paciente",

                currentRoute =
                    Routes.PATIENT_EDIT,

                showBack =
                    true,

                onBackClick = {

                    backRequestKey++
                }
            ) { innerPadding ->


                PatientFormScreen(

                    patientId =
                        patientId,

                    backRequestKey =
                        backRequestKey,

                    onBackConfirmed = {

                        navController.popBackStack()
                    },

                    onSaved = {

                        // Refresca listado
                        onPatientsChanged()

                        // Refresca detalle
                        onPatientDetailChanged()

                        // Regresa
                        navController.popBackStack()
                    },

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * MÉDICOS
         * =================================================
         */

        composable(
            route =
                Routes.DOCTORS
        ) {

            AppScreenScaffold(

                title =
                    "Médicos",

                currentRoute =
                    Routes.DOCTORS,

                showMenu =
                    true,

                onMenuClick =
                    onOpenDrawer
            ) { innerPadding ->


                DoctorsScreen(

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * ESPECIALIDADES
         * =================================================
         */

        composable(
            route =
                Routes.SPECIALITIES
        ) {

            AppScreenScaffold(

                title =
                    "Especialidades",

                currentRoute =
                    Routes.SPECIALITIES,

                showMenu =
                    true,

                onMenuClick =
                    onOpenDrawer
            ) { innerPadding ->


                SpecialitiesScreen(

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }


        /*
         * =================================================
         * AGENDA MÉDICA
         * =================================================
         */

        composable(
            route =
                Routes.MEDICAL_SCHEDULES
        ) {

            AppScreenScaffold(

                title =
                    "Agenda médica",

                currentRoute =
                    Routes.MEDICAL_SCHEDULES,

                showMenu =
                    true,

                onMenuClick =
                    onOpenDrawer
            ) { innerPadding ->


                MedicalSchedulesScreen(

                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }
    }
}


/*
 * =====================================================
 * NAVEGACIÓN ENTRE PANTALLAS PRINCIPALES
 * =====================================================
 */
private fun navigateMainRoute(

    navController: NavHostController,

    currentRoute: String?,

    route: String
) {


    // Si ya estamos en esa pantalla,
    // no volvemos a abrirla
    if (currentRoute == route) {
        return
    }


    /*
     * =================================================
     * HOME
     * =================================================
     */

    if (route == Routes.HOME) {


        val returnedHome =
            navController.popBackStack(

                route =
                    Routes.HOME,

                inclusive =
                    false
            )


        if (!returnedHome) {

            navController.navigate(
                Routes.HOME
            ) {

                launchSingleTop =
                    true
            }
        }


        return
    }


    /*
     * =================================================
     * RESTO DE RUTAS PRINCIPALES
     * =================================================
     */

    navController.navigate(
        route
    ) {

        launchSingleTop =
            true

        popUpTo(
            Routes.HOME
        ) {

            inclusive =
                false
        }
    }
}