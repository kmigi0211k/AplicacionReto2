
package com.example.reto2.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reto2.ui.home.HomeScreen
import com.example.reto2.ui.login.LoginScreen
import com.example.reto2.ui.login.LoginViewModel

object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.HOME) {
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
                    }
                }
            )
        }
    }
}


