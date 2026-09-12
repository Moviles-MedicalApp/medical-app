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
import pe.com.smart.presentation.appointments.AppointmentsScreen
import pe.com.smart.presentation.doctors.DoctorsScreen
import pe.com.smart.presentation.home.HomeScreen
import pe.com.smart.presentation.login.LoginScreen
import pe.com.smart.presentation.patients.PatientsScreen
import pe.com.smart.presentation.patients.detail.PatientDetailScreen
import pe.com.smart.presentation.patients.form.PatientFormScreen
import pe.com.smart.presentation.schedules.MedicalSchedulesScreen
import pe.com.smart.presentation.specialities.SpecialitiesScreen
import pe.com.smart.presentation.splash.SplashScreen

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

    val drawerRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.PATIENTS,
            Routes.DOCTORS,
            Routes.SPECIALITIES,
            Routes.MEDICAL_SCHEDULES
        )

    val bottomBarRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.PATIENTS
        )

    val authenticatedRoutes =
        setOf(
            Routes.HOME,
            Routes.APPOINTMENTS,
            Routes.PATIENTS,
            Routes.PATIENT_CREATE,
            Routes.PATIENT_DETAIL,
            Routes.PATIENT_EDIT,
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
            .collect { message ->

                /*
                 * Ocultamos cualquier mensaje anterior
                 * antes de mostrar el nuevo.
                 */
                showUiMessage =
                    false

                delay(150)

                currentUiMessage =
                    message

                showUiMessage =
                    true

                /*
                 * Duración según el tipo.
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

                delay(duration)

                /*
                 * Animación de salida.
                 */
                showUiMessage =
                    false

                delay(300)

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

                onPatientsChanged = {

                    patientsRefreshKey++
                },

                onPatientDetailChanged = {

                    patientDetailRefreshKey++
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
                                TokenStorage.clearToken()

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

                    onPatientsChanged = {

                        patientsRefreshKey++
                    },

                    onPatientDetailChanged = {

                        patientDetailRefreshKey++
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
    onPatientsChanged: () -> Unit,
    onPatientDetailChanged: () -> Unit,
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

                    /*
                     * Perfil posteriormente.
                     */
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

                    onNewAppointmentClick = {

                        /*
                         * Próximamente.
                         */
                    },

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

            AppScreenScaffold(
                title =
                    "Nuevo paciente",

                currentRoute =
                    Routes.PATIENT_CREATE,

                showBack =
                    true,

                onBackClick = {

                    navController.popBackStack()
                }
            ) { innerPadding ->

                PatientFormScreen(
                    patientId =
                        null,

                    onSaved = {

                        onPatientsChanged()

                        /*
                         * Notificación personalizada.
                         */
                        UiMessageManager.success(
                            title =
                                "Paciente registrado",

                            message =
                                "El paciente fue guardado correctamente."
                        )

                        navController
                            .popBackStack()
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

                        UiMessageManager.success(
                            title =
                                "Paciente eliminado",

                            message =
                                "El registro fue eliminado correctamente."
                        )

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

            AppScreenScaffold(
                title =
                    "Editar paciente",

                currentRoute =
                    Routes.PATIENT_EDIT,

                showBack =
                    true,

                onBackClick = {

                    navController.popBackStack()
                }
            ) { innerPadding ->

                PatientFormScreen(
                    patientId =
                        patientId,

                    onSaved = {

                        onPatientsChanged()

                        onPatientDetailChanged()

                        UiMessageManager.success(
                            title =
                                "Paciente actualizado",

                            message =
                                "Los cambios se guardaron correctamente."
                        )

                        navController
                            .popBackStack()
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