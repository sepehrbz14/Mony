package com.tolou.mony.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.tolou.mony.data.SessionStorage
import com.tolou.mony.data.network.AuthApi
import com.tolou.mony.data.network.ExpenseApi
import com.tolou.mony.data.network.RetrofitInstance
import com.tolou.mony.data.network.SmsApi
import com.tolou.mony.data.network.SmsRetrofitInstance
import com.tolou.mony.data.network.UserApi
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.ExpenseRepository
import com.tolou.mony.ui.data.SmsRepository
import com.tolou.mony.ui.data.UserRepository
import com.tolou.mony.ui.screens.login.LoginScreen
import com.tolou.mony.ui.screens.login.LoginState
import com.tolou.mony.ui.screens.login.LoginViewModel
import com.tolou.mony.ui.screens.login.LoginViewModelFactory
import com.tolou.mony.ui.screens.login.VerifyCodeScreen
import com.tolou.mony.ui.screens.main.MainScreen
import com.tolou.mony.ui.screens.main.MainViewModel
import com.tolou.mony.ui.screens.main.MainViewModelFactory
import com.tolou.mony.ui.screens.settings.SettingsScreen
import com.tolou.mony.ui.screens.transaction.AddTransactionScreen
import com.tolou.mony.ui.screens.transaction.TransactionType
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    onLoggedOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sessionStorage = remember { SessionStorage(context) }
    var username by remember { mutableStateOf(sessionStorage.fetchUsername().orEmpty()) }
    val authRepository = remember {
        AuthRepository(
            RetrofitInstance.retrofit.create(AuthApi::class.java),
            sessionStorage
        )
    }
    val smsRepository = remember {
        SmsRepository(
            SmsRetrofitInstance.retrofit.create(SmsApi::class.java)
        )
    }
    val authViewModelFactory = remember { LoginViewModelFactory(authRepository, smsRepository) }
    val expenseRepository = remember {
        ExpenseRepository(
            RetrofitInstance.retrofit.create(ExpenseApi::class.java),
            authRepository
        )
    }
    val userRepository = remember {
        UserRepository(
            RetrofitInstance.retrofit.create(UserApi::class.java),
            authRepository
        )
    }
    val coroutineScope = rememberCoroutineScope()
    var isSavingUsername by remember { mutableStateOf(false) }
    var saveUsernameError by remember { mutableStateOf<String?>(null) }

    val startDestination = if (authRepository.token().isNullOrBlank()) {
        NavRoutes.Login.route
    } else {
        NavRoutes.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN (enter phone)
        composable(NavRoutes.Login.route) {
            val viewModel: LoginViewModel = viewModel(factory = authViewModelFactory)
            LaunchedEffect(viewModel.state) {
                if (viewModel.state is LoginState.CodeSent) {
                    viewModel.consumeCodeSent()
                    navController.navigate(NavRoutes.Verify.route)
                }
            }
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(
                        NavRoutes.Main.route
                    ) {
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )

        }

        // VERIFY SIGNUP OTP
        composable(NavRoutes.Verify.route) {
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(NavRoutes.Login.route)
            }
            val viewModel: LoginViewModel = viewModel(
                parentEntry,
                factory = authViewModelFactory
            )
            VerifyCodeScreen(
                viewModel = viewModel,
                onVerified = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // MAIN
        composable(NavRoutes.Main.route) {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(expenseRepository)
            )
            MainScreen(
                viewModel = viewModel,
                username = username,
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onAddTransactionClick = {
                    navController.navigate(NavRoutes.AddTransaction.route)
                }
            )
        }

        composable(NavRoutes.AddTransaction.route) {
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(NavRoutes.Main.route)
            }
            val viewModel: MainViewModel = viewModel(
                parentEntry,
                factory = MainViewModelFactory(expenseRepository)
            )
            AddTransactionScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { type, amount, category, description ->
                    val title = if (description.isBlank()) {
                        category
                    } else {
                        "$category: $description"
                    }
                    when (type) {
                        TransactionType.Income -> viewModel.addIncome(title, amount)
                        TransactionType.Expense -> viewModel.addExpense(title, amount)
                    }
                    navController.popBackStack()
                }
            )
        }

        // SETTINGS
        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                username = username,
                onUsernameChange = { updatedName ->
                    username = updatedName
                },
                onSave = {
                    coroutineScope.launch {
                        saveUsernameError = null
                        isSavingUsername = true
                        try {
                            val response = userRepository.updateUsername(username)
                            username = response.username.orEmpty()
                            sessionStorage.saveUsername(username)
                        } catch (e: Exception) {
                            saveUsernameError = e.localizedMessage ?: "Failed to save username."
                        } finally {
                            isSavingUsername = false
                        }
                    }
                },
                isSaving = isSavingUsername,
                saveError = saveUsernameError,
                onLogout = {
                    authRepository.clearSession()
                    username = ""
                    onLoggedOut()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Main.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
