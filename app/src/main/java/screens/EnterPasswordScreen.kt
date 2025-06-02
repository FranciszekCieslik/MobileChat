package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPasswordScreen(
    navController: NavController,
    roomId: String
) {
    val firestore = Firebase.firestore
    val passwordInput = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val correctPassword = remember { mutableStateOf<String?>(null) }

    // 1) Pobierz wzorcowe hasło z Firestore dla tego roomId
    LaunchedEffect(roomId) {
        firestore.collection("rooms")
            .document(roomId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    correctPassword.value = doc.getString("password")
                } else {
                    // Dokument pokoju nie istnieje (pewnie ktoś skasował)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Room nie istnieje.")
                    }
                }
            }
            .addOnFailureListener { e ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Błąd pobierania pokoju: ${e.localizedMessage}")
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Password", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                value = passwordInput.value,
                onValueChange = { passwordInput.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val entered = passwordInput.value
                val stored = correctPassword.value

                when {
                    stored == null -> {
                        // Jeszcze nie pobraliśmy hasła albo pokój nie istnieje
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Oczekiwanie na hasło lub pokój nie istnieje.")
                        }
                    }
                    entered == stored -> {
                        // Hasło poprawne → przejdź do czatu
                        navController.navigate("chat/$roomId") {
                            // Usuń ten ekran z backstack, żeby po wejściu do czatu
                            // przyciśnięcie „Back” nie wracało nagle tutaj
                            popUpTo("enterPassword/$roomId") { inclusive = true }
                        }
                    }
                    else -> {
                        // Błędne hasło: pokaż komunikat
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Niepoprawne hasło",
                                actionLabel = "Spróbuj ponownie"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                passwordInput.value = ""
                            }
                        }
                    }
                }
            }) {
                Text("Submit")
            }
        }
    }
}
