package com.palette.mobile.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.palette.mobile.android.ui.auth.AuthViewModel
import com.palette.mobile.android.ui.auth.LoginScreen
import com.palette.mobile.android.ui.auth.RegisterScreen
import com.palette.mobile.android.ui.collection.CollectionScreen
import com.palette.mobile.android.ui.collection.CollectionViewModel
import com.palette.mobile.android.ui.create.CreatePaletteScreen
import com.palette.mobile.android.ui.create.CreatePaletteViewModel
import com.palette.mobile.android.ui.detail.PaletteDetailScreen
import com.palette.mobile.android.ui.detail.PaletteDetailViewModel
import com.palette.mobile.android.ui.discover.DiscoverScreen
import com.palette.mobile.android.ui.discover.DiscoverViewModel
import com.palette.mobile.android.ui.profile.ProfileScreen
import com.palette.mobile.android.ui.profile.ProfileViewModel

sealed class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector) {
    data object Discover : BottomNavItem(Screen.Discover, "Discover", Icons.Default.Explore)
    data object Create : BottomNavItem(Screen.Create, "Create", Icons.Default.AddCircleOutline)
    data object Collection : BottomNavItem(Screen.Collection, "Collection", Icons.Default.Favorite)
    data object Profile : BottomNavItem(Screen.Profile, "Profile", Icons.Default.AccountCircle)
}

@Composable
fun PaletteNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Discover,
        BottomNavItem.Create,
        BottomNavItem.Collection,
        BottomNavItem.Profile
    )

    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Discover.route) {
                val viewModel: DiscoverViewModel = hiltViewModel()
                DiscoverScreen(
                    viewModel = viewModel,
                    onPaletteClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onAuthRequired = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Detail.route) {
                val viewModel: PaletteDetailViewModel = hiltViewModel()
                PaletteDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAuthRequired = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Create.route) {
                val viewModel: CreatePaletteViewModel = hiltViewModel()
                CreatePaletteScreen(
                    viewModel = viewModel,
                    onSuccess = { id ->
                        navController.navigate(Screen.Detail.createRoute(id)) {
                            popUpTo(Screen.Discover.route)
                        }
                    },
                    onAuthRequired = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Collection.route) {
                val viewModel: CollectionViewModel = hiltViewModel()
                CollectionScreen(
                    viewModel = viewModel,
                    onPaletteClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onAuthRequired = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onRegisterClick = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Login.route) {
                val viewModel: AuthViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onSuccess = { navController.popBackStack() },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Register.route) {
                val viewModel: AuthViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onSuccess = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
