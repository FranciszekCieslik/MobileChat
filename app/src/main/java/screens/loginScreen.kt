package screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MobileChat.MainActivity
import com.example.MobileChat.MainProvider

@SuppressLint("ContextCastToActivity")
@Composable
fun LoginScreen(
    navController: NavController,
    provider: MainProvider = viewModel()
) {
    val state = provider.state.collectAsState()
    LocalContext.current as? MainActivity
        ?: throw IllegalStateException("MainActivity is required for LoginScreen")

    val isEmailValid = state.value.email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
    val isPasswordValid = state.value.password.length >= 8

    val error by remember { mutableStateOf<String?>(null) }
    val isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign In", style = MaterialTheme.typography.displayMedium)

        Spacer(modifier = Modifier.height(16.dp))
        EmailField(
            value = state.value.email,
            onValueChange = { provider.onEmailChange(it) },
            isEmailValid
        )
        PasswordField(
            value = state.value.password,
            onValueChange = { provider.onPasswordChange(it) },
            isPasswordValid
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.value.error?.let {
            Text(text = it, color = Color.Red)
        }

        Button(
            onClick = { provider.signIn(navController) },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.value.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Sign In")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        error?.let {
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.navigate("register") },
//                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create an account")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

//        Button(
//            onClick = { activity.startSignIn()},
//            modifier = Modifier
//                .padding(16.dp)
//                .border (2.dp, Color.Black, RoundedCornerShape(18.dp)) // Zaokrąglona granica
//                .clip(RoundedCornerShape(8.dp)), // Zaokrąglenie granic przycisku
//            colors = ButtonDefaults.buttonColors(Color.White)
//        ){
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Ikona Google
//                androidx.compose.foundation.Image(
//                    painter = painterResource(id = R.drawable.icon_google),
//                    contentDescription = "Google Logo",
//                    modifier = Modifier.padding(end = 8.dp)
//                )
//                Text(text = "Sign in  with Google", color = Color.Black)
//            }
//        }

    }
}