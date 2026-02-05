package com.tolou.mony.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tolou.mony.data.ExpenseDatabase
import com.tolou.mony.ui.screens.login.LoginScreen
import com.tolou.mony.ui.screens.login.VerifyCodeScreen
import com.tolou.mony.ui.screens.main.MainScreen
import com.tolou.mony.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    database: ExpenseDatabase
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        // LOGIN (enter phone)
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onCodeSent = { phone ->
                    navController.navigate(
                        NavRoutes.Verify.createRoute(phone)
                    )
                }
            )
        }

        // VERIFY OTP
        composable(
            route = NavRoutes.Verify.route,
            arguments = listOf(
                navArgument("phone") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone")!!

            VerifyCodeScreen(
                phone = phone,
                onVerified = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // MAIN
        composable(NavRoutes.Main.route) {
            MainScreen(
                database = database,
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                }
            )
        }

        // SETTINGS
        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
