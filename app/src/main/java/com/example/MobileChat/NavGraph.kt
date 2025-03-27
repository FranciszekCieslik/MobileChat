package com.example.MobileChat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import authentication.RegisterViewModel
import screens.ChatsScreen
import screens.EditProfileScreen
import screens.MainScreen
import screens.LoadingScreen
import screens.RegisterScreen
import screens.ProfileScreen
import screens.SettingsScreen
import screens.LoginScreen

@Composable
fun NavGraph(navController: NavHostController, viewModel: RegisterViewModel) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController, viewModel) }
        composable("login") { LoginScreen(navController,viewModel) }
        composable("register") { RegisterScreen(navController,viewModel) }
        composable("main") { MainScreen(navController)}
        composable("profile") { ProfileScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController, viewModel) }
        composable("editprofile") { EditProfileScreen(navController)}
        composable("chats") { ChatsScreen(navController)}
    }
}