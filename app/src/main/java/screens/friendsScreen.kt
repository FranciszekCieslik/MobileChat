package screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MobileChat.MainProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController, provider: MainProvider = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Search", "Received", "Sent", "Friends")
    Scaffold(
        bottomBar = { BottomNavBar(navController, selectedRoute = "friends") },
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> SearchUserTab(provider)
                1 -> ReceivedInvitesTab(provider)
                2 -> SentInvitesTab(provider)
                3 -> FriendsListTab(provider)
            }
        }
    }
}

@Composable
fun SearchUserTab(provider: MainProvider = viewModel()) {
    var query by remember { mutableStateOf("") }
    var filteredResults by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(query) {
        filteredResults = provider.getFilteredInviteCandidates(query = query)
    }

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by email or nickname") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Results:", style = MaterialTheme.typography.titleMedium)

        filteredResults.forEach { result ->
            ListItem(
                headlineContent = { Text(result) },
                trailingContent = {
                    Button(onClick = {

                        val isEmail = result.contains("@")
                        val removeFromList: (String) -> Unit = { toRemove ->
                            filteredResults = filteredResults.filterNot { it == toRemove }
                        }

                        if (isEmail) {
                            provider.getUserIdByEmail(result) { receiverId ->
                                receiverId?.let {
                                    provider.sendFriendRequest(it)
                                    removeFromList(result)
                                }
                            }
                        } else {
                            provider.getUserIdByNickname(result) { receiverId ->
                                receiverId?.let {
                                    provider.sendFriendRequest(it)
                                    removeFromList(result)
                                }
                            }
                        }
                    }) {
                        Text("Invite")
                    }
                }
            )
            HorizontalDivider()
        }
    }

}


@Composable
fun ReceivedInvitesTab(provider: MainProvider = viewModel()) {
    var receivedInvites by remember { mutableStateOf<List<String>>(emptyList()) }
    var userNameCache by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    // Pobranie zaproszeń
    LaunchedEffect(Unit) {
        val invites = provider.getFriendRequests()
        receivedInvites = invites
    }

    // Pobranie nazw użytkowników
    LaunchedEffect(receivedInvites) {
        val updatedCache = userNameCache.toMutableMap()
        receivedInvites.forEach { senderId ->
            if (!updatedCache.containsKey(senderId)) {
                val name = provider.getUserName(senderId)
                updatedCache[senderId] = name
            }
        }
        userNameCache = updatedCache
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Received Invites:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(receivedInvites) { senderId ->
                val displayName = userNameCache[senderId] ?: "Wczytywanie..."

                ListItem(
                    headlineContent = { Text(displayName) },
                    trailingContent = {
                        Row {
                            Button(onClick = {
                                coroutineScope.launch {
                                    provider.acceptFriendRequest(senderId)
                                    receivedInvites = receivedInvites.filterNot { it == senderId }
                                }
                            }) {
                                Text("Accept")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = {
                                coroutineScope.launch {
                                    provider.cancelFriendRequest(senderId)
                                    receivedInvites = receivedInvites.filterNot { it == senderId }
                                }
                            }) {
                                Text("Decline")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}



@Composable
fun SentInvitesTab(provider: MainProvider = viewModel()) {
    var sentInvites by remember { mutableStateOf<List<String>>(emptyList()) }
    var userNameCache by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        sentInvites = provider.getInvitedFriends()
    }

    LaunchedEffect(sentInvites) {
        val updatedCache = userNameCache.toMutableMap()
        sentInvites.forEach { receiverId ->
            if (!updatedCache.containsKey(receiverId)) {
                val name = provider.getUserName(receiverId)
                updatedCache[receiverId] = name
            }
        }
        userNameCache = updatedCache
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Sent Invites:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(sentInvites) { receiverId ->
                val displayName = userNameCache[receiverId] ?: "Wczytywanie..."

                ListItem(
                    headlineContent = { Text(displayName) },
                    trailingContent = {
                        OutlinedButton(onClick = {
                            coroutineScope.launch {
                                provider.cancelSendFriendRequest(receiverId)
                                sentInvites = sentInvites.filterNot { it == receiverId }
                            }
                        }) {
                            Text("Cancel")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun FriendsListTab(provider: MainProvider = viewModel()) {
    var friends by remember { mutableStateOf<List<String>>(emptyList()) }
    var userNameCache by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        friends = provider.getFriends()
    }

    LaunchedEffect(friends) {
        val updatedCache = userNameCache.toMutableMap()
        friends.forEach { friendId ->
            if (!updatedCache.containsKey(friendId)) {
                val name = provider.getUserName(friendId)
                updatedCache[friendId] = name
            }
        }
        userNameCache = updatedCache
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Friends:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(friends) { friendId ->
                val displayName = userNameCache[friendId] ?: "Wczytywanie..."

                ListItem(
                    headlineContent = { Text(displayName) },
                    trailingContent = {
                        IconButton(onClick = {
                            provider.removeFriend(friendId)
                            friends = friends.filterNot { it == friendId }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove friend")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
