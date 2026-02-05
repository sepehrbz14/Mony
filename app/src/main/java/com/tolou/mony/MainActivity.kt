package com.tolou.mony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tolou.mony.data.DatabaseProvider
import com.tolou.mony.ui.navigation.AppNavGraph
import com.tolou.mony.ui.screens.main.MainScreen
import com.tolou.mony.ui.theme.MonyTheme
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import com.tolou.mony.data.ExpenseDatabase
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.data.network.RetrofitInstance
import com.tolou.mony.ui.data.AuthApi
import com.tolou.mony.ui.screens.login.LoginScreen
import com.tolou.mony.ui.screens.login.LoginState
import com.tolou.mony.ui.screens.login.LoginViewModel
import com.tolou.mony.ui.screens.login.VerifyCodeScreen
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val database by lazy {
        ExpenseDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MonyTheme {
                AppNavGraph(
                    database = database
                )
            }
        }
    }
}

