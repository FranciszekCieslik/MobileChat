// app/src/main/java/screens/BrowseRoomsScreen.kt
package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.MobileChat.RoomResponse
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseRoomsScreen(navController: NavController) {
    val firestore = Firebase.firestore
    // Teraz RoomResponse ma: id, name, secure, password
    val rooms = remember { mutableStateOf<List<RoomResponse>>(emptyList()) }

    // Nasłuch na kolekcję „rooms”
    LaunchedEffect(Unit) {
        firestore.collection("rooms").addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            snapshot?.let { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val secure = doc.getBoolean("secure") ?: false
                    // Hasło może być null, jeśli „secure == false”
                    val password = doc.getString("password")
                    RoomResponse(
                        id = doc.id,
                        name = name,
                        secure = secure,
                        password = password
                    )
                }
                rooms.value = list
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Rooms", fontSize = 20.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (rooms.value.isEmpty()) {
                Text("No rooms available", fontSize = 16.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rooms.value) { room ->
                        RoomItem(
                            room = room,
                            onJoinClick = {
                                if (room.secure) {
                                    // Jeżeli pokój zabezpieczony, najpierw hasło
                                    navController.navigate("enterPassword/${room.id}")
                                } else {
                                    // Natychmiast do czatu
                                    navController.navigate("chat/${room.id}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: RoomResponse, onJoinClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokazujemy nazwę + jeśli zabezpieczony, dopisujemy „(🔒)”
            Text(
                text = if (room.secure) "${room.name} (🔒)" else room.name,
                fontSize = 18.sp
            )
            Button(onClick = onJoinClick) {
                Text("Join")
            }
        }
    }
}
