package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.MobileChat.MainProvider

@Composable
fun RegisterScreen(
    navController: NavController,
    provider: MainProvider = viewModel()
) {
    val state = provider.state.collectAsState()
    var termsAccepted by remember { mutableStateOf(false) }
    val isEmailValid = state.value.email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
    val isPasswordValid = state.value.password.length >= 8
    val isFormValid = isEmailValid && isPasswordValid && termsAccepted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create an Account", style = MaterialTheme.typography.displayMedium)

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

        // Checkbox akceptacji regulaminu
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Text(
                text = "I accept the regulations and privacy policy",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (!termsAccepted) {
            Text(
                text = "You must accept the regulations",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.value.error?.let {
            Text(text = it, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {provider.signUp(navController)},
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
            ) {
            if (state.value.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Sign up")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Text("Already have an account? Sign in")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

}
@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit, isEmailValid:Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )
    if (value.isNotEmpty() && !isEmailValid) {
        Text(
            text = "The e-mail address is incorrect",
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, isPasswordValid: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        isError = value.isNotEmpty() && !isPasswordValid
    )

    if (value.isNotEmpty() && !isPasswordValid) {
        Text(
            text = "password must be at least 8 characters long",
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
