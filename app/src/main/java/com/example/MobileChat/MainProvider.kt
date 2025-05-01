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
    private fun createUserMappings(email: String, nickname: String?, userId: String) {
        // mapowanie email
        db.collection("emailToId").document(email).set(mapOf("userId" to userId))

        // mapowanie nickname, jeśli istnieje
        nickname?.let {
            db.collection("nicknameToId").document(it).set(mapOf("userId" to userId))
        }
    }

    fun getUserIdByEmail(email: String, onResult: (String?) -> Unit) {
        db.collection("emailToId").document(email).get()
            .addOnSuccessListener { doc ->
                val userId = doc.getString("userId")
                onResult(userId)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun getUserIdByNickname(nickname: String, onResult: (String?) -> Unit) {
        db.collection("nicknameToId").document(nickname).get()
            .addOnSuccessListener { doc ->
                val userId = doc.getString("userId")
                onResult(userId)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserProfile(name: String?, bio: String?, profileUrl: String?) {
        val user = Firebase.auth.currentUser
        val userId = user?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val oldNickname = document.getString("name") ?: ""

                    val updates = mutableMapOf<String, Any>()
                    name?.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
                    bio?.takeIf { it.isNotBlank() }?.let { updates["bio"] = it }
                    profileUrl?.takeIf { it.isNotBlank() }?.let { updates["profile_url"] = it }

                    // Usuń poprzedni nickname z mapowania
                    if (oldNickname.isNotBlank()) {
                        db.collection("nicknameToId").document(oldNickname).delete()
                    }

                    // Zaktualizuj dane użytkownika
                    db.collection("users").document(userId).update(updates)
                        .addOnSuccessListener {
                            fetchUserData(userId)
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainProvider", "Failed to update user profile", e)
                        }

                    // Dodaj nowe mapowanie (jeśli podano nowe imię)
                    if (name != null) {
                        val email = user.email.toString()
                        createUserMappings(email, name, userId)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Błąd podczas pobierania danych użytkownika", e)
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
                    createUserMappings(_state.value.email,"", Firebase.auth.currentUser?.uid.toString())
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

    fun removeUser(navController: NavController, password: String) {
        val user = Firebase.auth.currentUser ?: return
        val email = user.email ?: run {
            Log.e(TAG, "No email found for current user.")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                val db = FirebaseFirestore.getInstance()
                val userId = user.uid
                val userDocRef = db.collection("users").document(userId)

                // Najpierw pobieramy dane użytkownika
                userDocRef.get().addOnSuccessListener { doc ->
                    val nickname = doc.getString("nickname")

                    // Usuń dokument użytkownika
                    userDocRef.delete()

                    // Usuń mapowania
                    db.collection("emailToId").document(email).delete()
                    if (nickname != null) {
                        db.collection("nicknameToId").document(nickname).delete()
                    }

                    // Usuń zdjęcie profilowe ze Storage
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("profile_images/$userId.jpg")
                    storageRef.delete()

                    // Usuń konto użytkownika
                    user.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d(TAG, "User fully deleted.")
                            signOut(navController)
                        } else {
                            Log.e(TAG, "User deletion failed", deleteTask.exception)
                        }
                    }

                }.addOnFailureListener {
                    Log.e(TAG, "Failed to get user document", it)
                }

            } else {
                Log.e(TAG, "Reauthentication failed", reauthTask.exception)
            }
        }
    }

    fun fetchAllEmails(onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("emailToId")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val emails = querySnapshot.documents.mapNotNull { it.id }
                onResult(emails)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Błąd przy pobieraniu emaili: ", exception)
                onResult(emptyList())
            }
    }

    fun fetchAllNicknames(onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("nicknameToId")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val nicknames = querySnapshot.documents.mapNotNull { it.id }
                onResult(nicknames)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Błąd przy pobieraniu nickname'ów: ", exception)
                onResult(emptyList())
            }
    }


}
