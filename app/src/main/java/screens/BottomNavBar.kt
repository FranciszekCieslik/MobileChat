package screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.MobileChat.R

@Composable
fun BottomNavBar(navController: NavController, selectedRoute: String) {
    val items = listOf(
        BottomNavItem("main", "Home", Icons.Default.Home),
        BottomNavItem("chats", "Chats", R.drawable.baseline_chat_24),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    when (val icon = item.icon) {
                        is ImageVector -> Icon(icon, contentDescription = item.label)
                        is Int -> Icon(
                            painter = painterResource(id = icon),
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("main") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: Any)
