package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController, selectedRoute = "main") },
        topBar = {
            TopAppBar(
                title = {
                    Text("MobileChat", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Main Content", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("createRoom") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Room")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("browseRooms") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Browse New Rooms")
            }
        }
    }
}
