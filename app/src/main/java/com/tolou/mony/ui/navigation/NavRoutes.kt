package com.tolou.mony.ui.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Main : NavRoutes("main")
    object AddTransaction : NavRoutes("add-transaction")
    object Settings : NavRoutes("settings")
    object Verify : NavRoutes("verify")
}
