
package com.example.reto2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reto2.ui.admin.AdminScreen
import com.example.reto2.ui.home.HomeScreen
import com.example.reto2.ui.home.HomeViewModel
import com.example.reto2.ui.login.LoginScreen
import com.example.reto2.ui.login.LoginViewModel
import com.example.reto2.ui.profile.ProfileScreen

object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val ADMIN = "admin"
    const val PROFILE = "profile"
    const val CAMERA = "camera"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val loginViewModel: LoginViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    val authState by loginViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val destination =
                        if (authState.user?.role == "admin") {
                            AppRoutes.ADMIN
                        } else {
                            AppRoutes.HOME
                        }

                    navController.navigate(destination) {
                        popUpTo(AppRoutes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(AppRoutes.PROFILE) {
                        launchSingleTop = true
                    }
                },
                onOpenCamera = {
                    navController.navigate(AppRoutes.CAMERA) {
                        launchSingleTop = true
                    }
                },
                homeViewModel = homeViewModel
            )
        }

        composable(AppRoutes.ADMIN) {
            AdminScreen(
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.ADMIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppRoutes.CAMERA) {
            CamaraScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
