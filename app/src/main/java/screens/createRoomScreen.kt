package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(navController: NavController) {
    val roomName = remember { mutableStateOf("") }
    val isSecure = remember { mutableStateOf(false) }
    val password = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Room") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            OutlinedTextField(
                value = roomName.value,
                onValueChange = { roomName.value = it },
                label = { Text("Room Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSecure.value,
                    onCheckedChange = { isSecure.value = it }
                )
                Text("Secure Room")
            }
            if (isSecure.value) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (roomName.value.isNotBlank()) {
                        // Pobierz aktualnego użytkownika z Firebase Auth
                        val currentUser = Firebase.auth.currentUser
                        if (currentUser != null) {
                            // Przygotuj dane pokoju
                            val roomData = hashMapOf(
                                "name" to roomName.value,
                                "secure" to isSecure.value,
                                "password" to if (isSecure.value) password.value else null,
                                "createdBy" to currentUser.uid,
                                "users" to listOf(currentUser.uid)
                            )
                            // Zapisz pokój w Firestore
                            Firebase.firestore.collection("rooms")
                                .add(roomData)
                                .addOnSuccessListener { documentReference ->
                                    message.value = "Room created successfully (ID: ${documentReference.id})"
                                    // Opcjonalnie: nawigacja do pokoju lub powrót do ekranu głównego
                                }
                                .addOnFailureListener { e ->
                                    message.value = "Error creating room: ${e.localizedMessage}"
                                }
                        } else {
                            message.value = "User not authenticated"
                        }
                    } else {
                        message.value = "Room name cannot be empty"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Room")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (message.value.isNotBlank()) {
                Text(message.value)
            }
        }
    }
}

