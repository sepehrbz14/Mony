package com.tolou.mony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tolou.mony.ui.navigation.AppNavGraph
import com.tolou.mony.ui.theme.MonyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MonyTheme {
                AppNavGraph(
                    onLoggedOut = {}
                )
            }
        }
    }
}
