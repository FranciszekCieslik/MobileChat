package com.example.MobileChat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import screens.BrowseRoomsScreen
import screens.ChatsScreen
import screens.CreateRoomScreen
import screens.EditProfileScreen
import screens.EnterPasswordScreen
import screens.FriendsScreen
import screens.LoadingScreen
import screens.LoginScreen
import screens.ProfileScreen
import screens.RegisterScreen
import screens.SettingsScreen
import screens.ChatScreen

@Composable
fun NavGraph(navController: NavHostController, provider: MainProvider) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController, provider) }
        composable("login") { LoginScreen(navController, provider) }
        composable("register") { RegisterScreen(navController, provider) }
        composable("chats") { ChatsScreen(navController) }
        composable("friends") { FriendsScreen(navController, provider) }
        composable("profile") { ProfileScreen(navController, provider) }
        composable("settings") { SettingsScreen(navController, provider) }
        composable("createRoom") { CreateRoomScreen(navController) }
        composable("browseRooms") { BrowseRoomsScreen(navController) }
        composable("editprofile") { EditProfileScreen(navController, provider) }
        composable("enterPassword/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            EnterPasswordScreen(navController, roomId)
        }
        composable("chat/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            ChatScreen(navController, roomId)
        }
    }
}