package com.palette.mobile.android.ui.navigation

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Create : Screen("create")
    data object Collection : Screen("collection")
    data object Profile : Screen("profile")
    data object Detail : Screen("detail/{paletteId}") {
        fun createRoute(paletteId: String) = "detail/$paletteId"
    }
    data object Login : Screen("login")
    data object Register : Screen("register")
}
