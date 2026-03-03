package com.example.tutorial.com.example.tutorial.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tutorial.com.example.tutorial.presentation.feature.auth.AuthViewModel
import com.example.tutorial.com.example.tutorial.presentation.feature.profile.AccountInfoScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.article.ArticleDetailScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.profile.DeleteAccountScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.tips.HealthTipsScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.home.HomeScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.auth.LoginScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.profile.MyPageScreen
import com.example.tutorial.com.example.tutorial.presentation.feature.settings.SettingsScreen

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Default.Home)
    object HealthTips : NavigationItem("health_tips", "Health Tips", Icons.Default.Favorite)
    object MyPage : NavigationItem("my_page", "My Page", Icons.Default.Person)
    object Login : NavigationItem("login", "Login", Icons.Default.Person)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationGraph(navController: NavHostController) {

    val activity = LocalContext.current as ComponentActivity
    val authVM: AuthViewModel = hiltViewModel(activity)

    val isLoggedIn = authVM.ui.collectAsState().value.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        val target = if (isLoggedIn)
            NavigationItem.Home.route
        else
            NavigationItem.Login.route

        navController.navigate(target) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }


    NavHost(
        navController = navController,
        startDestination = NavigationItem.Login.route,
        enterTransition = { fadeIn(tween(0)) },
        exitTransition = { fadeOut(tween(0)) },
        popEnterTransition = { fadeIn(tween(0)) },
        popExitTransition = { fadeOut(tween(0)) }
    ) {

        composable(NavigationItem.Login.route) {
            LoginScreen(vm = authVM)
        }
        composable(NavigationItem.Home.route) { HomeScreen() }
        composable(NavigationItem.HealthTips.route) { HealthTipsScreen(navController) }

        composable(NavigationItem.MyPage.route) {
            MyPageScreen(navController, authVM)
        }
        composable("my_page/account_info") { AccountInfoScreen() }
        composable("my_page/settings") { SettingsScreen() }
        composable("my_page/delete_account") {
            DeleteAccountScreen(navController)
        }

        composable("health_tips/detail/{id}") { backStack ->
            ArticleDetailScreen(backStack.arguments?.getString("id") ?: "")
        }
    }
}
