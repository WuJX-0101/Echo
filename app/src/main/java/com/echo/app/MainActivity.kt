package com.echo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.echo.app.data.repository.SettingsPreferences
import com.echo.app.ui.navigation.EchoNavGraph
import com.echo.app.ui.theme.EchoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsPrefs = remember { SettingsPreferences(this@MainActivity) }
            val preferDark by settingsPrefs.darkMode.collectAsState(initial = false)

            EchoTheme(darkTheme = if (preferDark) true else isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    EchoNavGraph(navController = navController)
                }
            }
        }
    }
}
