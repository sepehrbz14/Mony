package com.tolou.mony.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tolou.mony.data.ExpenseDatabase
import com.tolou.mony.data.network.RetrofitInstance
import com.tolou.mony.ui.data.AuthApi
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.screens.login.LoginScreen
import com.tolou.mony.ui.screens.login.LoginViewModel
import com.tolou.mony.ui.screens.login.LoginViewModelFactory
import com.tolou.mony.ui.screens.login.VerifyCodeScreen
import com.tolou.mony.ui.screens.main.MainScreen
import com.tolou.mony.ui.screens.settings.SettingsScreen
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavGraph(
    database: ExpenseDatabase
) {
    val navController = rememberNavController()
    val authRepository = remember {
        AuthRepository(
            RetrofitInstance.retrofit.create(AuthApi::class.java)
        )
    }
    val authViewModelFactory = remember { LoginViewModelFactory(authRepository) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        // LOGIN (enter phone)
        composable(NavRoutes.Login.route) {
            val viewModel: LoginViewModel = viewModel(factory = authViewModelFactory)
            LoginScreen(
                viewModel = viewModel,
                onCodeSent = { phone ->
                    navController.navigate(
                        NavRoutes.Verify.createRoute(phone)
                    )
                },
                onLoggedIn = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
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
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(NavRoutes.Login.route)
            }
            val viewModel: LoginViewModel = viewModel(
                parentEntry,
                factory = authViewModelFactory
            )
            val phone = requireNotNull(backStackEntry.arguments?.getString("phone")) {
                "Phone number missing from verification route."
            }

            VerifyCodeScreen(
                phone = phone,
                viewModel = viewModel,
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
