package pe.com.smart

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import pe.com.smart.di.appModule
import pe.com.smart.presentation.navigation.AppNavigation
import pe.com.smart.core.ui.theme.SmartTheme

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        SmartTheme {
            AppNavigation()
        }
    }
}
