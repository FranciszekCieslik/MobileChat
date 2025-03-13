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
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    Log.d(TAG, user?.displayName.toString())
                    navController.navigate("login"){_state.value.isLoading = false}
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    navController.navigate("register")
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
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    auth.currentUser
                    navController.navigate("main"){getUserData()}
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    navController.navigate("login")
                }
            }
    }

    fun signOut(navController: NavController){
        _state.value = RegisterState() // Reset to initial state
        Firebase.auth.signOut()
        navController.navigate("login")

    }

    fun checkUserSession(navController: NavController) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Użytkownik jest zalogowany, przejdź do głównego ekranu
            navController.navigate("main"){getUserData()}
        } else {
            // Użytkownik niezalogowany, przejdź do ekranu logowania
            navController.navigate("register")
        }
    }

    fun removeUser(navController: NavController){
        val user = Firebase.auth.currentUser!!
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User account deleted.")
                }
            }
        signOut(navController)
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