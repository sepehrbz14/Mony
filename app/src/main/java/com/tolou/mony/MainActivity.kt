package com.tolou.mony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tolou.mony.data.ExpenseDatabase
import com.tolou.mony.ui.navigation.AppNavGraph
import com.tolou.mony.ui.theme.MonyTheme

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
