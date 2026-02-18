package com.tolou.mony.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tolou.mony.notifications.NotificationAccessHelper
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
import com.tolou.mony.data.network.IncomeApi
import com.tolou.mony.data.network.RetrofitInstance
import com.tolou.mony.data.network.SmsApi
import com.tolou.mony.data.network.SmsRetrofitInstance
import com.tolou.mony.data.network.UserApi
import com.tolou.mony.notifications.PendingTransactionStore
import com.tolou.mony.ui.data.AuthRepository
import com.tolou.mony.ui.data.ExpenseRepository
import com.tolou.mony.ui.data.IncomeRepository
import com.tolou.mony.ui.data.SmsRepository
import com.tolou.mony.ui.data.UserRepository
import com.tolou.mony.ui.screens.login.LoginScreen
import com.tolou.mony.ui.screens.login.LoginViewModel
import com.tolou.mony.ui.screens.login.LoginViewModelFactory
import com.tolou.mony.ui.screens.login.SignUpScreen
import com.tolou.mony.ui.screens.login.VerifyCodeScreen
import com.tolou.mony.ui.screens.login.WelcomeScreen
import com.tolou.mony.ui.screens.main.MainScreen
import com.tolou.mony.ui.screens.main.MainViewModel
import com.tolou.mony.ui.screens.main.MainViewModelFactory
import com.tolou.mony.ui.screens.pending.PendingTransactionsScreen
import com.tolou.mony.ui.screens.settings.SettingsScreen
import com.tolou.mony.ui.screens.transaction.AddTransactionScreen
import com.tolou.mony.ui.screens.transaction.TransactionType
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    isDarkModeEnabled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLoggedOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val sessionStorage = remember { SessionStorage(context) }
    val pendingStore = remember { PendingTransactionStore(context) }
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
    val incomeRepository = remember {
        IncomeRepository(
            RetrofitInstance.retrofit.create(IncomeApi::class.java),
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
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }
    var changePasswordError by remember { mutableStateOf<String?>(null) }
    var changePasswordSuccess by remember { mutableStateOf<String?>(null) }
    var isNotificationAccessEnabled by remember {
        mutableStateOf(NotificationAccessHelper.isNotificationListenerEnabled(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationAccessEnabled =
                    NotificationAccessHelper.isNotificationListenerEnabled(context)
                if (isNotificationAccessEnabled) {
                    NotificationAccessHelper.ensureListenerRunning(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val startDestination = if (authRepository.token().isNullOrBlank()) {
        NavRoutes.Welcome.route
    } else {
        NavRoutes.Main.route
    }

    LaunchedEffect(startDestination) {
        if (startDestination == NavRoutes.Main.route) {
            try {
                val profile = userRepository.fetchProfile()
                username = profile.username.orEmpty()
                sessionStorage.saveUsername(username)
            } catch (_: Exception) {
                // Keep existing cached username.
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(NavRoutes.Welcome.route) {
            WelcomeScreen(
                onLogin = { navController.navigate(NavRoutes.Login.route) },
                onSignUp = { navController.navigate(NavRoutes.SignUp.route) }
            )
        }

        // LOGIN (enter phone)
        composable(NavRoutes.Login.route) {
            val viewModel: LoginViewModel = viewModel(factory = authViewModelFactory)
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    coroutineScope.launch {
                        try {
                            val profile = userRepository.fetchProfile()
                            username = profile.username.orEmpty()
                            sessionStorage.saveUsername(username)
                        } catch (_: Exception) {
                            username = sessionStorage.fetchUsername().orEmpty()
                        }
                    }
                    navController.navigate(
                        NavRoutes.Main.route
                    ) {
                        popUpTo(NavRoutes.Welcome.route) {
                            inclusive = true
                        }
                    }
                },
                onSignUp = { navController.navigate(NavRoutes.SignUp.route) },
                onBack = { navController.popBackStack() }
            )

        }

        // SIGN UP (enter phone)
        composable(NavRoutes.SignUp.route) {
            val viewModel: LoginViewModel = viewModel(factory = authViewModelFactory)
            SignUpScreen(
                viewModel = viewModel,
                onCodeSent = { navController.navigate(NavRoutes.Verify.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // VERIFY SIGNUP OTP
        composable(NavRoutes.Verify.route) {
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(NavRoutes.SignUp.route)
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
                factory = MainViewModelFactory(expenseRepository, incomeRepository)
            )
            MainScreen(
                viewModel = viewModel,
                username = username,
                onSettingsClick = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onPendingClick = {
                    navController.navigate(NavRoutes.PendingTransactions.route)
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
                factory = MainViewModelFactory(expenseRepository, incomeRepository)
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

        composable(NavRoutes.PendingTransactions.route) {
            val pendingItems = remember { mutableStateOf(pendingStore.getAll()) }
            PendingTransactionsScreen(
                items = pendingItems.value,
                onBack = { navController.popBackStack() },
                onItemClick = { item ->
                    navController.popBackStack()
                    val intent = Intent(context, com.tolou.mony.notifications.SmsTransactionPromptActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(com.tolou.mony.notifications.SmsTransactionPromptActivity.EXTRA_AMOUNT, item.amount)
                        putExtra(com.tolou.mony.notifications.SmsTransactionPromptActivity.EXTRA_TRANSACTION_TYPE, item.type.name)
                        putExtra(com.tolou.mony.notifications.SmsTransactionPromptActivity.EXTRA_PENDING_ID, item.id)
                    }
                    context.startActivity(intent)
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
                isDarkModeEnabled = isDarkModeEnabled,
                onDarkModeToggle = onDarkModeChange,
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
                currentPassword = currentPassword,
                onCurrentPasswordChange = { currentPassword = it },
                newPassword = newPassword,
                onNewPasswordChange = { newPassword = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                onChangePassword = {
                    coroutineScope.launch {
                        changePasswordError = null
                        changePasswordSuccess = null
                        isChangingPassword = true
                        try {
                            val response = userRepository.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword
                            )
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            changePasswordSuccess = response.message
                        } catch (e: Exception) {
                            changePasswordError =
                                e.localizedMessage ?: "Failed to update password."
                        } finally {
                            isChangingPassword = false
                        }
                    }
                },
                isChangingPassword = isChangingPassword,
                changePasswordError = changePasswordError,
                changePasswordSuccess = changePasswordSuccess,
                isNotificationAccessEnabled = isNotificationAccessEnabled,
                onNotificationAccessClick = {
                    val intent = NotificationAccessHelper.buildSettingsIntent()
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                },
                onLogout = {
                    authRepository.clearSession()
                    username = ""
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    changePasswordError = null
                    changePasswordSuccess = null
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
