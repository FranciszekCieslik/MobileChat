package authentication
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state
    private var auth = Firebase.auth

    // Aktualizowanie emaila
    fun onEmailChange(newEmail: String) {
        _state.value = _state.value.copy(email = newEmail)
    }

    // Aktualizowanie hasła
    fun onPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(password = newPassword)
    }

    fun signUp(navController: NavController) {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.value = _state.value.copy(error = "Email and password cannot be empty.")
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(_state.value.email, _state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    _state.value = _state.value.copy(isLoading = false) // Resetuje stan po zakończeniu

                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true } // Usuwa ekran rejestracji
                        launchSingleTop = true
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    _state.value = _state.value.copy(isLoading = false, error = "Registration failed")
                }
            }
    }

    fun signIn(navController: NavController) {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.value = _state.value.copy(error = "Email and password cannot be empty.")
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        auth.signInWithEmailAndPassword(_state.value.email, _state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    getUserData() // Pobierz dane użytkownika przed nawigacją

                    navController.navigate("chats") {
                        popUpTo("login") { inclusive = true } // Usuwa ekran logowania
                        launchSingleTop = true
                    }

                    _state.value = _state.value.copy(isLoading = false)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    _state.value = _state.value.copy(isLoading = false, error = "Login failed")
                }
            }
    }

    fun signOut(navController: NavController) {
        _state.value = RegisterState() // Resetowanie stanu użytkownika
        Firebase.auth.signOut()
        navController.navigate("login") {
            popUpTo("chats") { inclusive = true }
            launchSingleTop = true
        }
    }

    fun checkUserSession(navController: NavController) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            getUserData()
            navController.navigate("chats") {
                popUpTo("loading") { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate("register") {
                popUpTo("loading") { inclusive = true }
                launchSingleTop = true
            }
        }
    }


    fun removeUser(navController: NavController) {
        val user = Firebase.auth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "User account deleted.")
                signOut(navController)
            } else {
                Log.e(TAG, "User deletion failed", task.exception)
            }
        }
    }


    private fun getUserData(){
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                _state.value = _state.value.copy(name = profile.displayName.toString())
                _state.value = _state.value.copy(email = profile.email.toString())
                _state.value = _state.value.copy(photoUrl = profile.photoUrl.toString())

            }
        }
    }
}