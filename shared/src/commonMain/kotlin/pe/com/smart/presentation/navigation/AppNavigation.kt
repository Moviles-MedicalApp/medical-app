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
import kotlinx.coroutines.launch
import pe.com.smart.core.ui.AppDrawer
import pe.com.smart.core.ui.AppMessageDialog
import pe.com.smart.core.ui.AppScreenScaffold
import pe.com.smart.core.ui.AppSnackbar
import pe.com.smart.core.ui.message.UiMessage
import pe.com.smart.core.ui.message.UiMessageManager
import pe.com.smart.data.local.SessionManager
import pe.com.smart.data.local.TokenStorage
import pe.com.smart.data.local.AccessTokenClaimsDecoder
import pe.com.smart.data.remote.ApiProvider
import pe.com.smart.data.repository.AuthRepository
import pe.com.smart.presentation.appointments.AppointmentsScreen
import pe.com.smart.presentation.appointments.AppointmentCalendarScreen
import pe.com.smart.presentation.appointments.DoctorAgendaScreen
import pe.com.smart.presentation.appointments.PatientAppointmentsScreen
import pe.com.smart.presentation.doctors.DoctorsScreen
import pe.com.smart.presentation.doctors.detail.DoctorDetailScreen
import pe.com.smart.presentation.doctors.form.DoctorFormScreen
import pe.com.smart.presentation.home.HomeScreen
import pe.com.smart.presentation.login.LoginScreen
import pe.com.smart.presentation.login.RegistrationScreen
import pe.com.smart.presentation.patients.PatientsScreen
import pe.com.smart.presentation.patients.detail.PatientDetailScreen
import pe.com.smart.presentation.patients.form.PatientFormScreen
import pe.com.smart.presentation.schedules.MedicalSchedulesScreen
import pe.com.smart.presentation.schedules.ScheduleManagementScreen
import pe.com.smart.presentation.specialities.SpecialitiesScreen
import pe.com.smart.presentation.specialities.detail.SpecialityDetailScreen
import pe.com.smart.presentation.specialities.form.SpecialityFormScreen
import pe.com.smart.presentation.splash.SplashScreen
import pe.com.smart.presentation.users.UsersScreen
import pe.com.smart.presentation.users.UserProvisioningSelection

import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation() {

    val navController =
        rememberNavController()

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
     * REFRESCO DE MÉDICOS
     * ==================================================
     */

    var doctorsRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    var doctorDetailRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    /*
     * ==================================================
     * REFRESCO DE ESPECIALIDADES
     * ==================================================
     */

    var specialitiesRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    var specialityDetailRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    /*
     * ==================================================
     * RUTA ACTUAL
     * ==================================================
     */

    val backStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry
            ?.destination
            ?.route

    val tokenClaims = AccessTokenClaimsDecoder.decode(TokenStorage.getToken().orEmpty())
    val isAdministrator = tokenClaims?.hasRole("ADMIN") == true
    val isDoctor = tokenClaims?.hasRole("DOCTOR") == true

    val drawerRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.APPOINTMENT_CALENDAR,
            Routes.PATIENTS,
            Routes.DOCTORS,
            Routes.SPECIALITIES,
            Routes.MEDICAL_SCHEDULES,
            Routes.USERS
        )

    val bottomBarRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.APPOINTMENT_CALENDAR,
            Routes.PATIENTS
        )

    val authenticatedRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.APPOINTMENT_CALENDAR,
            Routes.PATIENTS,
            Routes.PATIENT_CREATE,
            Routes.PATIENT_DETAIL,
            Routes.PATIENT_EDIT,
            Routes.DOCTORS,
            Routes.SPECIALITIES,
            Routes.SPECIALITY_CREATE,
            Routes.SPECIALITY_DETAIL,
            Routes.SPECIALITY_EDIT,
            Routes.MEDICAL_SCHEDULES,
            Routes.USERS
        )

    val administratorRoutes = setOf(
        Routes.PATIENTS,
        Routes.PATIENT_CREATE,
        Routes.PATIENT_DETAIL,
        Routes.PATIENT_EDIT,
        Routes.DOCTORS,
        Routes.DOCTOR_CREATE,
        Routes.DOCTOR_DETAIL,
        Routes.DOCTOR_EDIT,
        Routes.SPECIALITIES,
        Routes.SPECIALITY_CREATE,
        Routes.SPECIALITY_DETAIL,
        Routes.SPECIALITY_EDIT,
        Routes.MEDICAL_SCHEDULES,
        Routes.MEDICAL_SCHEDULE_MANAGE,
        Routes.USERS
    )

    LaunchedEffect(currentRoute, isAdministrator) {
        if (currentRoute in administratorRoutes && !isAdministrator) {
            navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = false } }
        }
    }

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

                /*
                 * Cerramos la notificación anterior.
                 */
                showUiMessage =
                    false

                delay(120)

                /*
                 * Establecemos la nueva.
                 */
                currentUiMessage =
                    message

                /*
                 * Entrada animada.
                 */
                showUiMessage =
                    true

                /*
                 * Duración.
                 */
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

                delay(
                    duration
                )

                /*
                 * Salida animada.
                 */
                showUiMessage =
                    false

                delay(
                    250
                )

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

                /*
                 * Evitamos procesar varios
                 * eventos 401 simultáneamente.
                 */
                if (handlingSessionExpiration) {
                    return@collect
                }

                /*
                 * Si ya estamos en Login,
                 * no hacemos nada.
                 */
                if (
                    navController
                        .currentDestination
                        ?.route == Routes.LOGIN
                ) {
                    return@collect
                }

                handlingSessionExpiration =
                    true

                /*
                 * Cerramos Drawer.
                 */
                if (drawerState.isOpen) {

                    drawerState.snapTo(
                        DrawerValue.Closed
                    )
                }

                /*
                 * Eliminamos token.
                 */
                TokenStorage.clearToken()

                /*
                 * Ocultamos cualquier notificación.
                 */
                showUiMessage =
                    false

                currentUiMessage =
                    null

                /*
                 * Navegamos al Login.
                 */
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

                /*
                 * Modal de sesión expirada.
                 */
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

                specialitiesRefreshKey =
                    specialitiesRefreshKey,

                specialityDetailRefreshKey =
                    specialityDetailRefreshKey,

                doctorsRefreshKey =
                    doctorsRefreshKey,

                doctorDetailRefreshKey =
                    doctorDetailRefreshKey,

                onPatientsChanged = {

                    patientsRefreshKey++
                },

                onPatientDetailChanged = {

                    patientDetailRefreshKey++
                },

                onSpecialitiesChanged = {

                    specialitiesRefreshKey++
                },

                onSpecialityDetailChanged = {

                    specialityDetailRefreshKey++
                },

                onDoctorsChanged = {

                    doctorsRefreshKey++
                },

                onDoctorDetailChanged = {

                    doctorDetailRefreshKey++
                },

                onOpenDrawer = {},

                isAdministrator = isAdministrator
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
                        currentRoute = currentRoute,
                        username = tokenClaims?.username
                            ?.ifBlank { "Usuario" } ?: "Usuario",
                        role = when {
                            isAdministrator -> "Administrador"
                            isDoctor -> "Médico"
                            tokenClaims?.hasRole("EMPLOYEE") == true ->
                                "Empleado"
                            else -> "Paciente"
                        },
                        isAdministrator = isAdministrator,
                        drawerState = drawerState,
                        onNavigate = { route ->

                            scope.launch {

                                /*
                                 * Primero cerramos
                                 * completamente el Drawer.
                                 */
                                drawerState.close()

                                /*
                                 * Luego navegamos.
                                 */
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

                                /*
                                 * Logout manual:
                                 * no mostrar modal.
                                 */
                                showSessionExpiredDialog =
                                    false

                                handlingSessionExpiration =
                                    false

                                /*
                                 * Ocultamos cualquier
                                 * notificación.
                                 */
                                showUiMessage =
                                    false

                                currentUiMessage =
                                    null

                                /*
                                 * Cerramos Drawer.
                                 */
                                drawerState.close()

                                /*
                                 * Eliminamos token.
                                 */
                                AuthRepository(ApiProvider.authApiService).logoutRemotely()

                                /*
                                 * Vamos al Login.
                                 */
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

                    specialitiesRefreshKey =
                        specialitiesRefreshKey,

                    specialityDetailRefreshKey =
                        specialityDetailRefreshKey,

                    doctorsRefreshKey =
                        doctorsRefreshKey,

                    doctorDetailRefreshKey =
                        doctorDetailRefreshKey,

                    onPatientsChanged = {

                        patientsRefreshKey++
                    },

                    onPatientDetailChanged = {

                        patientDetailRefreshKey++
                    },

                    onSpecialitiesChanged = {

                        specialitiesRefreshKey++
                    },

                    onSpecialityDetailChanged = {

                        specialityDetailRefreshKey++
                    },

                    onDoctorsChanged = {

                        doctorsRefreshKey++
                    },

                    onDoctorDetailChanged = {

                        doctorDetailRefreshKey++
                    },

                    onOpenDrawer = {

                        if (showDrawer) {

                            scope.launch {

                                drawerState.open()
                            }
                        }
                    },
                    isAdministrator = isAdministrator
                )
            }
        }

        /*
         * ==================================================
         * NOTIFICACIÓN GLOBAL PERSONALIZADA
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
     * MODAL GLOBAL DE SESIÓN EXPIRADA
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
 */

@Composable
private fun AppNavHost(
    navController: NavHostController,
    patientsRefreshKey: Int,
    patientDetailRefreshKey: Int,
    specialitiesRefreshKey: Int,
    specialityDetailRefreshKey: Int,
    doctorsRefreshKey: Int,
    doctorDetailRefreshKey: Int,
    onPatientsChanged: () -> Unit,
    onPatientDetailChanged: () -> Unit,
    onSpecialitiesChanged: () -> Unit,
    onSpecialityDetailChanged: () -> Unit,
    onDoctorsChanged: () -> Unit,
    onDoctorDetailChanged: () -> Unit,
    onOpenDrawer: () -> Unit,
    isAdministrator: Boolean,
    modifier: Modifier = Modifier
) {

    val sessionClaims = AccessTokenClaimsDecoder.decode(TokenStorage.getToken().orEmpty())
    val isDoctor = sessionClaims?.hasRole("DOCTOR") == true
    // A clinical account can retain a PATIENT claim. The clinical role must take
    // precedence so its navigation never falls back to the patient experience.
    val isPatient = sessionClaims?.hasRole("PATIENT") == true && !isDoctor

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
                    val destination = if (TokenStorage.hasToken() &&
                        AuthRepository(ApiProvider.authApiService).refresh().isSuccess
                    ) Routes.HOME else Routes.LOGIN

                    navController.navigate(
                        destination
                    ) {

                        popUpTo(
                            Routes.SPLASH
                        ) {

                            inclusive = true
                        }

                        launchSingleTop = true
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

                        launchSingleTop = true
                    }
                },
                onRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegistrationScreen(
                onRegistered = {
                    UiMessageManager.success("Cuenta creada", "Ahora inicia sesión.")
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.USERS) {
            AppScreenScaffold(
                title = "Usuarios",
                currentRoute = Routes.USERS,
                showMenu = true,
                onMenuClick = onOpenDrawer
            ) { innerPadding ->
                UsersScreen(Modifier.padding(innerPadding))
            }
        }


        /*
         * =====================================================
         * HOME
         * =====================================================
         */

        composable(
            route = Routes.HOME
        ) {
            /*
             * Configuración visual del encabezado
             * según los roles del usuario autenticado.
             */

            val homeTitle = when {
                isAdministrator -> "Administración"
                isDoctor -> "Panel médico"
                isPatient -> "Mi salud"
                else -> "Inicio"
            }

            val homeSubtitle = when {
                isAdministrator -> "Gestión del sistema"
                isDoctor -> "Gestión de atenciones"
                isPatient -> "Mis citas médicas"
                else -> null
            }

            val username = sessionClaims
                ?.username
                ?.takeIf { it.isNotBlank() }
                ?: "Usuario"

            AppScreenScaffold(
                title = homeTitle,

                subtitle = homeSubtitle,

                username = username,

                currentRoute = Routes.HOME,

                showMenu = true,

                showProfile = true,

                showBottomBar = true,

                isAdministrator = isAdministrator,

                onMenuClick = onOpenDrawer,

                onProfileClick = {
                    /*
                     * La navegación al perfil se
                     * implementará cuando exista
                     * la ruta correspondiente.
                     */
                },

                onNavigate = { route ->
                    navigateMainRoute(
                        navController = navController,
                        currentRoute = Routes.HOME,
                        route = route
                    )
                }
            ) { innerPadding ->

                HomeScreen(
                    onAppointmentsClick = {
                        navigateMainRoute(
                            navController = navController,
                            currentRoute = Routes.HOME,
                            route = Routes.APPOINTMENTS
                        )
                    },

                    onPatientsClick = {
                        navigateMainRoute(
                            navController = navController,
                            currentRoute = Routes.HOME,
                            route = Routes.PATIENTS
                        )
                    },

                    onDoctorsClick = {
                        navigateMainRoute(
                            navController = navController,
                            currentRoute = Routes.HOME,
                            route = Routes.DOCTORS
                        )
                    },

                    onSpecialitiesClick = {
                        navigateMainRoute(
                            navController = navController,
                            currentRoute = Routes.HOME,
                            route = Routes.SPECIALITIES
                        )
                    },

                    onNewAppointmentClick = {
                        navController.navigate(
                            Routes.appointmentCreate()
                        )
                    },

                    onCalendarClick = {
                        navigateMainRoute(
                            navController = navController,
                            currentRoute = Routes.HOME,
                            route = Routes.APPOINTMENT_CALENDAR
                        )
                    },

                    modifier = Modifier.padding(
                        innerPadding
                    )
                )
            }
        }


        /*
         * =================================================
         * CITAS
         * =================================================
         */

        composable(
            route =
                Routes.APPOINTMENTS
        ) {

            AppScreenScaffold(
                title =
                    if (isDoctor) "Mi agenda" else "Citas",

                currentRoute =
                    Routes.APPOINTMENTS,

                showMenu =
                    true,

                showBottomBar =
                    true,

                isAdministrator = isAdministrator,

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

                if (isPatient) {
                    PatientAppointmentsScreen(
                        onBookAppointment = { navController.navigate(Routes.appointmentCreate()) },
                        modifier = Modifier.padding(innerPadding)
                    )
                } else if (isDoctor) {
                    DoctorAgendaScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    AppointmentsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }


        /*
         * =====================================================
         * APPOINTMENT CALENDAR
         * =====================================================
         */

        composable(
            route = Routes.APPOINTMENT_CALENDAR
        ) {

            AppScreenScaffold(
                title = "Calendario",

                currentRoute = Routes.APPOINTMENT_CALENDAR,

                showMenu = true,

                showBottomBar = true,

                isAdministrator = isAdministrator,

                onMenuClick = onOpenDrawer,

                onNavigate = { route ->

                    navigateMainRoute(
                        navController = navController,
                        currentRoute = Routes.APPOINTMENT_CALENDAR,
                        route = route
                    )
                }
            ) { innerPadding ->

                AppointmentCalendarScreen(
                    onNewAppointment = { date ->

                        navController.navigate(
                            Routes.appointmentCreate(
                                date = date
                            )
                        )
                    },

                    modifier = Modifier.padding(
                        innerPadding
                    )
                )
            }
        }


        composable(
            route = Routes.APPOINTMENT_CREATE,
            arguments = listOf(navArgument("patientId") {
                type = NavType.IntType
                defaultValue = -1
            }, navArgument("date") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.read { getIntOrNull("patientId") }
                ?.takeIf { it > 0 }
            val initialDate = backStackEntry.arguments?.read { getStringOrNull("date") }
                ?.takeIf { it.isNotBlank() }
            AppScreenScaffold(
                title = "Nueva cita",
                currentRoute = Routes.APPOINTMENTS,
                showBack = true,
                onBackClick = { navController.popBackStack() }
            ) { innerPadding ->
                AppointmentsScreen(
                    modifier = Modifier.padding(innerPadding),
                    initialCreate = true,
                    initialPatientId = patientId,
                    initialDate = initialDate,
                    onInitialCreateDismiss = { navController.popBackStack() }
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

                isAdministrator = isAdministrator,

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

            /*
             * Cada incremento representa
             * una solicitud de volver.
             */
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

                    /*
                     * No navegamos directamente.
                     *
                     * El formulario decidirá si
                     * necesita confirmación.
                     */
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

                    onNewAppointmentClick = { id ->
                        navController.navigate(Routes.appointmentCreate(id))
                    },

                    onDeleted = {

                        onPatientsChanged()

                        navController.popBackStack(
                            route = Routes.PATIENTS,
                            inclusive = false
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

                        /*
                         * Refrescamos listado.
                         */
                        onPatientsChanged()

                        /*
                         * Refrescamos detalle.
                         */
                        onPatientDetailChanged()

                        /*
                         * Volvemos al detalle.
                         */
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
                    refreshKey = doctorsRefreshKey,
                    onDoctorClick = { doctorId ->
                        navController.navigate(
                            Routes.doctorDetail(
                                doctorId
                            )
                        )
                    },
                    onCreateDoctorClick = {
                        navController.navigate(
                            Routes.DOCTOR_CREATE
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
         * NUEVO MÉDICO
         * =================================================
         */

        composable(
            route =
                Routes.DOCTOR_CREATE,

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
                    "Nuevo médico",

                currentRoute =
                    Routes.DOCTOR_CREATE,

                showBack =
                    true,

                onBackClick = {
                    backRequestKey++
                }
            ) { innerPadding ->

                DoctorFormScreen(
                    doctorId = null,
                    backRequestKey = backRequestKey,
                    onBackConfirmed = {
                        navController.popBackStack()
                    },
                    onSaved = {
                        onDoctorsChanged()
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        /*
         * =================================================
         * DETALLE DEL MÉDICO
         * =================================================
         */

        composable(
            route =
                Routes.DOCTOR_DETAIL,

            arguments =
                listOf(
                    navArgument("doctorId") {
                        type = NavType.IntType
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
            val doctorId =
                backStackEntry
                    .arguments
                    ?.read {
                        getIntOrNull("doctorId")
                    }
                    ?: return@composable

            AppScreenScaffold(
                title =
                    "Detalle del médico",

                currentRoute =
                    Routes.DOCTOR_DETAIL,

                showBack =
                    true,

                onBackClick = {
                    navController.popBackStack()
                }
            ) { innerPadding ->

                DoctorDetailScreen(
                    doctorId = doctorId,
                    refreshKey = doctorDetailRefreshKey,
                    onEdit = { id ->
                        navController.navigate(
                            Routes.doctorEdit(id)
                        )
                    },
                    onProvisionAccount = { doctor ->
                        UserProvisioningSelection.select(
                            profileId = doctor.id,
                            role = "DOCTOR",
                            profileName = doctor.fullName,
                            specialityId = doctor.specialityId,
                            email = doctor.email
                        )
                        navController.navigate(Routes.USERS)
                    },
                    onDeleted = {
                        onDoctorsChanged()
                        navController.popBackStack(
                            route = Routes.DOCTORS,
                            inclusive = false
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        /*
         * =================================================
         * EDITAR MÉDICO
         * =================================================
         */

        composable(
            route =
                Routes.DOCTOR_EDIT,

            arguments =
                listOf(
                    navArgument("doctorId") {
                        type = NavType.IntType
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
            val doctorId =
                backStackEntry
                    .arguments
                    ?.read {
                        getIntOrNull("doctorId")
                    }
                    ?: return@composable

            var backRequestKey by
            remember {
                mutableIntStateOf(0)
            }

            AppScreenScaffold(
                title =
                    "Editar médico",

                currentRoute =
                    Routes.DOCTOR_EDIT,

                showBack =
                    true,

                onBackClick = {
                    backRequestKey++
                }
            ) { innerPadding ->

                DoctorFormScreen(
                    doctorId = doctorId,
                    backRequestKey = backRequestKey,
                    onBackConfirmed = {
                        navController.popBackStack()
                    },
                    onSaved = {
                        onDoctorsChanged()
                        onDoctorDetailChanged()
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(innerPadding)
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
                    refreshKey =
                        specialitiesRefreshKey,

                    onSpecialityClick = { specialityId ->

                        navController.navigate(
                            Routes.specialityDetail(
                                specialityId
                            )
                        )
                    },

                    onCreateSpecialityClick = {

                        navController.navigate(
                            Routes.SPECIALITY_CREATE
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
         * NUEVA ESPECIALIDAD
         * =================================================
         */

        composable(
            route =
                Routes.SPECIALITY_CREATE,

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
                    "Nueva especialidad",

                currentRoute =
                    Routes.SPECIALITY_CREATE,

                showBack =
                    true,

                onBackClick = {

                    backRequestKey++
                }
            ) { innerPadding ->

                SpecialityFormScreen(
                    specialityId =
                        null,

                    backRequestKey =
                        backRequestKey,

                    onBackConfirmed = {

                        navController.popBackStack()
                    },

                    onSaved = {

                        onSpecialitiesChanged()

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
         * DETALLE DE LA ESPECIALIDAD
         * =================================================
         */

        composable(
            route =
                Routes.SPECIALITY_DETAIL,

            arguments =
                listOf(

                    navArgument(
                        "specialityId"
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

            val specialityId =
                backStackEntry
                    .arguments
                    ?.read {

                        getIntOrNull(
                            "specialityId"
                        )
                    }
                    ?: return@composable

            AppScreenScaffold(
                title =
                    "Detalle de especialidad",

                currentRoute =
                    Routes.SPECIALITY_DETAIL,

                showBack =
                    true,

                onBackClick = {

                    navController.popBackStack()
                }
            ) { innerPadding ->

                SpecialityDetailScreen(
                    specialityId =
                        specialityId,

                    refreshKey =
                        specialityDetailRefreshKey,

                    onEditClick = { id ->

                        navController.navigate(
                            Routes.specialityEdit(
                                id
                            )
                        )
                    },

                    onDeleted = {

                        onSpecialitiesChanged()

                        navController.popBackStack(
                            route = Routes.SPECIALITIES,
                            inclusive = false
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
         * EDITAR ESPECIALIDAD
         * =================================================
         */

        composable(
            route =
                Routes.SPECIALITY_EDIT,

            arguments =
                listOf(

                    navArgument(
                        "specialityId"
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

            val specialityId =
                backStackEntry
                    .arguments
                    ?.read {

                        getIntOrNull(
                            "specialityId"
                        )
                    }
                    ?: return@composable

            var backRequestKey by
            remember {
                mutableIntStateOf(0)
            }

            AppScreenScaffold(
                title =
                    "Editar especialidad",

                currentRoute =
                    Routes.SPECIALITY_EDIT,

                showBack =
                    true,

                onBackClick = {

                    backRequestKey++
                }
            ) { innerPadding ->

                SpecialityFormScreen(
                    specialityId =
                        specialityId,

                    backRequestKey =
                        backRequestKey,

                    onBackConfirmed = {

                        navController.popBackStack()
                    },

                    onSaved = {

                        onSpecialitiesChanged()

                        onSpecialityDetailChanged()

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
                    onManageDoctor = { doctorId ->
                        navController.navigate(Routes.medicalScheduleManage(doctorId))
                    },
                    modifier =
                        Modifier.padding(
                            innerPadding
                        )
                )
            }
        }

        composable(
            route = Routes.MEDICAL_SCHEDULE_MANAGE,
            arguments = listOf(navArgument("doctorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.read { getIntOrNull("doctorId") }
                ?: return@composable
            AppScreenScaffold(
                title = "Gestionar horarios",
                currentRoute = Routes.MEDICAL_SCHEDULE_MANAGE,
                showBack = true,
                onBackClick = { navController.popBackStack() }
            ) { innerPadding ->
                ScheduleManagementScreen(doctorId, Modifier.padding(innerPadding))
            }
        }
    }
}


/*
 * =====================================================
 * NAVEGACIÓN PRINCIPAL
 * =====================================================
 */

private fun navigateMainRoute(
    navController: NavHostController,
    currentRoute: String?,
    route: String
) {

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
