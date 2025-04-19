package screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
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
                0 -> SearchUserTab()
                1 -> ReceivedInvitesTab()
                2 -> SentInvitesTab()
                3 -> FriendsListTab()
            }
        }
    }
}

@Composable
fun SearchUserTab() {
    var query by remember { mutableStateOf("") }
    val sampleResults = listOf("john@example.com", "alice123", "bob_smith")

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by email or username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Results:", style = MaterialTheme.typography.titleMedium)

        sampleResults.filter {
            it.contains(query, ignoreCase = true)
        }.forEach { result ->
            ListItem(
                headlineContent = { Text(result) },
                trailingContent = {
                    Button(onClick = { /* handle send invite */ }) {
                        Text("Invite")
                    }
                }
            )
            Divider()
        }
    }
}

@Composable
fun ReceivedInvitesTab() {
    val receivedInvites = listOf("jane_doe", "michael1988")

    Column {
        Text("Received Invites:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        receivedInvites.forEach { name ->
            ListItem(
                headlineContent = { Text(name) },
                trailingContent = {
                    Row {
                        Button(onClick = { /* handle accept */ }) {
                            Text("Accept")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { /* handle decline */ }) {
                            Text("Decline")
                        }
                    }
                }
            )
            Divider()
        }
    }
}

@Composable
fun SentInvitesTab() {
    val sentInvites = listOf("anna.w", "dave99")

    Column {
        Text("Sent Invites:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        sentInvites.forEach { name ->
            ListItem(
                headlineContent = { Text(name) },
                trailingContent = {
                    OutlinedButton(onClick = { /* handle cancel invite */ }) {
                        Text("Cancel")
                    }
                }
            )
            Divider()
        }
    }
}

@Composable
fun FriendsListTab() {
    val friends = listOf("charlie", "emily_92", "frank123")

    Column {
        Text("Friends:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        friends.forEach { name ->
            ListItem(
                headlineContent = { Text(name) },
                trailingContent = {
                    IconButton(onClick = { /* handle remove friend */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove friend")
                    }
                }
            )
            Divider()
        }
    }
}
