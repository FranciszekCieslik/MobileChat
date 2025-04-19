package com.example.MobileChat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import screens.BrowseRoomsScreen
import screens.ChatsScreen
import screens.CreateRoomScreen
import screens.EditProfileScreen
import screens.FriendsScreen
import screens.LoadingScreen
import screens.LoginScreen
import screens.ProfileScreen
import screens.RegisterScreen
import screens.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, provider: MainProvider) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController, provider) }
        composable("login") { LoginScreen(navController,provider) }
        composable("register") { RegisterScreen(navController,provider) }
        composable("chats") { ChatsScreen(navController)}
        composable("friends") { FriendsScreen(navController) }
        composable("profile") { ProfileScreen(navController, provider) }
        composable("settings") { SettingsScreen(navController, provider) }
        composable("createRoom") { CreateRoomScreen(navController) }
        composable("browseRooms") { BrowseRoomsScreen(navController) }
        composable("editprofile") { EditProfileScreen(navController)}
    }
}