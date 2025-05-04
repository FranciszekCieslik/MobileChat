package screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.MobileChat.MainProvider
import com.example.MobileChat.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(navController: NavController, provider: MainProvider) {
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var nickname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    val userState by provider.userState.collectAsState()
    LaunchedEffect(userState) {
        nickname = userState.name
        bio = TextFieldValue(userState.bio)
        profileImageUri = userState.profileUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            saveProfileData(provider, nickname, bio.text, profileImageUri)
                            navController.popBackStack()
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Done , contentDescription = "Save Changes")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zdjęcie profilowe
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = if (profileImageUri != null)
                        rememberAsyncImagePainter(profileImageUri)
                    else
                        painterResource(R.drawable.account_icon),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imageLauncher.launch("image/*") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Nickname
            val nicknameRegex = Regex("^[^.#\\[\\]/\$]{3,20}\$") // 3-20 znaków, bez .#[]
            val isNicknameValid = nickname.matches(nicknameRegex)
            val nicknameError = !isNicknameValid && nickname.isNotEmpty()

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                isError = nicknameError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (nicknameError) {
                Text(
                    text = "Nickname must be 3-20 characters long and cannot contain . # [ ] / \$",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Opis
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun saveProfileData(
    provider: MainProvider,
    name: String,
    bio: String,
    newImageUri: Uri?
) {
    val currentImageUrl = provider.userState.value.profileUrl

    if (newImageUri != null && newImageUri.toString() != currentImageUrl) {
        provider.uploadProfilePicture(newImageUri) { downloadUrl ->
            if (downloadUrl != null) {
                provider.updateUserProfile(name, bio, downloadUrl)
            } else {
                provider.updateUserProfile(name, bio, currentImageUrl)
            }
        }
    } else {
        provider.updateUserProfile(name, bio, currentImageUrl)
    }
}


