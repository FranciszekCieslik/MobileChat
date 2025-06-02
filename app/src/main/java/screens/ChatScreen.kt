// Ścieżka: app/src/main/java/screens/ChatScreen.kt
package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

// Model pojedynczej wiadomości
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    roomId: String
) {
    val firestore   = Firebase.firestore
    val storage     = Firebase.storage
    val auth        = Firebase.auth
    val currentUser = auth.currentUser
    val myUid       = currentUser?.uid ?: ""
    val myName      = currentUser?.displayName ?: "Ja"

    // Stan: lista wiadomości
    val messagesState = remember { mutableStateOf<List<Message>>(emptyList()) }
    // Stan: aktualny tekst wiadomości
    val newMessage    = remember { mutableStateOf("") }

    // Stan dla Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope    = rememberCoroutineScope()

    // Launcher do wyboru zdjęcia z galerii
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        // 1) Picker zwróci null, jeśli użytkownik anulował wybór
        uri?.let { pickedUri ->
            // 2) Pokaż URI w Snackbarze, żeby zweryfikować, że picker działa
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Wybrano URI obrazu: $pickedUri")
            }
            // 3) Przygotuj referencję w Firebase Storage
            val fileName   = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("chat_images/$roomId/$fileName")

            // 4) Wykonaj upload
            storageRef.putFile(pickedUri)
                .addOnSuccessListener {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Upload do Storage: OK")
                    }
                    // 5) Po sukcesie pobierz URL
                    storageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Pobrano downloadUrl: $downloadUri")
                            }
                            // 6) Zapisz dokument wiadomości z imageUrl
                            val msgMap = hashMapOf(
                                "text"       to "",
                                "senderId"   to myUid,
                                "senderName" to myName,
                                "imageUrl"   to downloadUri.toString(),
                                "timestamp"  to System.currentTimeMillis()
                            )
                            firestore.collection("rooms")
                                .document(roomId)
                                .collection("messages")
                                .add(msgMap)
                                .addOnSuccessListener {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Obraz zapisany w wiadomości OK")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Błąd zapisu wiadomości: ${exception.localizedMessage}"
                                        )
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "Nie udało się pobrać downloadUrl: ${exception.localizedMessage}"
                                )
                            }
                        }
                }
                .addOnFailureListener { exception ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            "Błąd uploadu do Storage: ${exception.localizedMessage}"
                        )
                    }
                }
        } ?: run {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Picker zwrócił null – zdjęcie nie wybrane.")
            }
        }
    }

    // Nasłuch na Firestore: kolekcja "rooms/{roomId}/messages"
    LaunchedEffect(roomId) {
        firestore.collection("rooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Błąd odczytu
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Błąd pobierania wiadomości: ${e.localizedMessage}")
                    }
                    return@addSnapshotListener
                }
                snapshot?.let { snap ->
                    val list = snap.documents.mapNotNull { doc ->
                        val text       = doc.getString("text") ?: ""
                        val senderId   = doc.getString("senderId") ?: ""
                        val senderName = doc.getString("senderName") ?: ""
                        val imageUrl   = doc.getString("imageUrl")
                        val timestamp  = doc.getLong("timestamp") ?: 0L
                        Message(
                            id = doc.id,
                            text = text,
                            senderId = senderId,
                            senderName = senderName,
                            imageUrl = imageUrl,
                            timestamp = timestamp
                        )
                    }
                    messagesState.value = list
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat Room", fontSize = 20.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // --- Wyświetlanie listy wiadomości ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messagesState.value) { msg ->
                    MessageRow(
                        message = msg,
                        isMine   = msg.senderId == myUid,
                        myName   = myName
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Pasek: emoji "📷" do wysłania zdjęcia + pole tekstowe + przycisk Wyślij ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1) Emoji zamiast ikony – do uruchomienia pickera
                Text(
                    text = "📷",
                    fontSize = 32.sp,
                    modifier = Modifier
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(4.dp)
                )

                // 2) Pole tekstowe do wpisywania wiadomości
                OutlinedTextField(
                    value = newMessage.value,
                    onValueChange = { newMessage.value = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Napisz wiadomość") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    singleLine = true
                )

                // 3) Przycisk Wyślij (tylko tekst)
                Button(
                    onClick = {
                        val text = newMessage.value.trim()
                        if (text.isNotEmpty() && currentUser != null) {
                            val msgMap = hashMapOf(
                                "text"       to text,
                                "senderId"   to myUid,
                                "senderName" to myName,
                                "timestamp"  to System.currentTimeMillis()
                            )
                            firestore.collection("rooms")
                                .document(roomId)
                                .collection("messages")
                                .add(msgMap)
                                .addOnSuccessListener {
                                    newMessage.value = ""
                                }
                                .addOnFailureListener { exception ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Błąd wysyłania: ${exception.localizedMessage}"
                                        )
                                    }
                                }
                        }
                    }
                ) {
                    Text("Wyślij")
                }
            }
        }
    }
}

@Composable
fun MessageRow(message: Message, isMine: Boolean, myName: String) {
    val bubbleColor = if (isMine) Color(0xFF7E57C2) else Color(0xFFBDBDBD)
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Wyświetl nick nad bąbelkiem
            Text(
                text = if (isMine) myName else message.senderName,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
            )

            // Jeśli imageUrl nie jest null/empty – wyświetl obraz, w przeciwnym razie – tekst
            if (!message.imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(bubbleColor)
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Obraz z czatu",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(bubbleColor)
                        .padding(8.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 16.sp,
                        color = if (isMine) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
