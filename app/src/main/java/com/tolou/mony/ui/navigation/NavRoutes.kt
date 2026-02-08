package com.tolou.mony.ui.navigation

sealed class NavRoutes(val route: String) {
    object Welcome : NavRoutes("welcome")
    object Login : NavRoutes("login")
    object SignUp : NavRoutes("sign-up")
    object Main : NavRoutes("main")
    object AddTransaction : NavRoutes("add-transaction")
    object Settings : NavRoutes("settings")
    object Verify : NavRoutes("verify")
}
