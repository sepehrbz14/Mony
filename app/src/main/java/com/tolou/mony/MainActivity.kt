package com.tolou.mony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.tolou.mony.data.SessionStorage
import com.tolou.mony.ui.navigation.AppNavGraph
import com.tolou.mony.notifications.NotificationAccessHelper
import com.tolou.mony.ui.theme.MonyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val sessionStorage = remember { SessionStorage(context) }
            remember(context) {
                NotificationAccessHelper.ensureListenerRunning(context)
                Unit
            }
            val systemDarkMode = isSystemInDarkTheme()
            var darkModeEnabled by remember {
                mutableStateOf(sessionStorage.fetchDarkModeEnabled() ?: systemDarkMode)
            }

            MonyTheme(darkTheme = darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        isDarkModeEnabled = darkModeEnabled,
                        onDarkModeChange = { enabled ->
                            darkModeEnabled = enabled
                            sessionStorage.saveDarkModeEnabled(enabled)
                        },
                        onLoggedOut = {}
                    )
                }
            }
        }
    }
}
