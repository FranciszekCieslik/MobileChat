// app/src/main/java/com/example/MobileChat/NavGraph.kt
package com.example.MobileChat

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import authentication.RegisterViewModel
import screens.*

@Composable
fun NavGraph(navController: NavHostController, viewModel: RegisterViewModel) {
    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") { LoadingScreen(navController, viewModel) }
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        composable("main") { MainScreen(navController) }
        composable("profile") { ProfileScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController, viewModel) }
        composable("createRoom") { CreateRoomScreen(navController) }
        composable("browseRooms") { BrowseRoomsScreen(navController) }

        // DODAJEMY TUTAJ nową composable dla hasła (parametr: roomId)
        composable("enterPassword/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            EnterPasswordScreen(navController = navController, roomId = roomId)
        }

        // Trasa do czatu (tu już zakładamy, że user ma prawo wchodzić – w tym przykładzie nie walidujemy ponownie hasła)
        composable("chat/{roomId}") { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            ChatScreen(navController = navController, roomId = roomId)
        }
    }
}
