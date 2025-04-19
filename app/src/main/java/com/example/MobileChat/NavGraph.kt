package com.example.MobileChat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import authentication.RegisterViewModel
import database.FirestoreDatabaseProvider
import screens.EditProfileScreen
import screens.ChatsScreen
import screens.LoadingScreen
import screens.RegisterScreen
import screens.ProfileScreen
import screens.SettingsScreen
import screens.LoginScreen
import screens.CreateRoomScreen
import screens.BrowseRoomsScreen
import screens.FriendsScreen

@Composable
fun NavGraph(navController: NavHostController, viewModel: RegisterViewModel, dbProvider: FirestoreDatabaseProvider) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController, viewModel) }
        composable("login") { LoginScreen(navController,viewModel) }
        composable("register") { RegisterScreen(navController,viewModel, dbProvider) }
        composable("chats") { ChatsScreen(navController)}
        composable("friends") { FriendsScreen(navController) }
        composable("profile") { ProfileScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController, viewModel) }
        composable("createRoom") { CreateRoomScreen(navController) }
        composable("browseRooms") { BrowseRoomsScreen(navController) }
        composable("editprofile") { EditProfileScreen(navController)}
    }
}