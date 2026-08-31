package pe.com.smart

import androidx.compose.runtime.Composable
import pe.com.smart.presentation.navigation.AppNavigation
import pe.com.smart.ui.theme.SmartTheme

@Composable
fun App() {
    SmartTheme {
        AppNavigation()
    }
}