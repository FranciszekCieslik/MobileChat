package com.example.MobileChat

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.MobileChat.states.RegisterState
import com.example.MobileChat.states.UserState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("UNCHECKED_CAST")
class MainProvider  : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState

    init {
        val userId = auth.currentUser?.uid
        userId?.let{
            fetchUserData(userId)
        }
    }

    //Storage
    fun uploadProfilePicture(imageUri: Uri, onComplete: (String?) -> Unit) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_images/$userId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    //DB
    fun updateUserProfileUrl(downloadUrl: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("profile_url", downloadUrl)
    }


    private fun deleteUserData(){
        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "User Firestore data deleted")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to delete Firestore user data", it)
            }

    }

    fun updateUserProfile(name: String?, bio: String?, profileUrl: String?) {
        val userId = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()

        name?.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
        bio?.takeIf { it.isNotBlank() }?.let { updates["bio"] = it }
        profileUrl?.takeIf { it.isNotBlank() }?.let { updates["profile_url"] = it }

        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                fetchUserData(userId) // odśwież stan po edycji
            }
            .addOnFailureListener { e ->
                Log.e("MainProvider", "Failed to update user profile", e)
            }
    }


    private fun fetchUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = UserState(
                        email = document.getString("email") ?: "",
                        name = document.getString("name") ?: "",
                        bio = document.getString("bio") ?: "",
                        profileUrl = document.getString("profile_url") ?: "",
                        friends = document.get("friends") as? List<String> ?: emptyList(),
                        invitedFriends = document.get("invited_friends") as? List<String> ?: emptyList(),
                        friendRequest = document.get("friend_requests") as? List<String> ?: emptyList()
                    )
                    _userState.value = user
                } else {
                    Log.w("MainProvider", "User document not found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainProvider", "Error fetching user data", exception)
            }
    }

    private fun addUserToFirestore() {
        val user = auth.currentUser
        val bio = ""
        user?.let {
            val userMap = hashMapOf(
                "email" to user.email,
                "name" to (user.displayName ?: ""),
                "profile_url" to (user.photoUrl?.toString() ?: ""),
                "friends" to emptyList<String>(),
                "friend_requests" to emptyList<String>(),
                "invited_friends" to emptyList<String>(),
                "bio" to bio
            )


            db.collection("users")
                .document(user.uid) // użyj UID jako ID dokumentu
                .set(userMap)       // zamiast .add()
                .addOnSuccessListener {
                    Log.d(TAG, "User document created with UID: ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding user document", e)
                }
        }
    }

    //AUTH

    fun onEmailChange(newEmail: String) {
        _state.value = _state.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(password = newPassword)
    }

    fun signUp(navController: NavController) {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.value = _state.value.copy(error = "Email and password cannot be empty.")

        }

        _state.value = _state.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(_state.value.email, _state.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addUserToFirestore()
                    Log.d(TAG, "createUserWithEmail:success")
                    _state.value = _state.value.copy(isLoading = false) // Resetuje stan po zakończeniu
                    val userId = auth.currentUser?.uid
                    userId?.let { fetchUserData(it) }
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

    fun removeUser(navController: NavController,password: String) {
        val user = Firebase.auth.currentUser

        if (user != null) {

            val email = Firebase.auth.currentUser?.email
            if (email == null) {
                Log.e(TAG, "No email found for current user.")
                return
            }

            val credential = EmailAuthProvider.getCredential(email, password)

            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    deleteUserData()
                    user.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d(TAG, "User account deleted.")
                            signOut(navController)
                        } else {
                            Log.e(TAG, "User deletion failed", deleteTask.exception)
                        }
                    }
                } else {
                    Log.e(TAG, "Reauthentication failed", reauthTask.exception)
                }
            }
        }
    }

    private fun getUserData(){
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
                _state.value = _state.value.copy(email = profile.email.toString())
            }
            val userId = auth.currentUser?.uid ?: return
            fetchUserData(userId)
        }
    }
}
