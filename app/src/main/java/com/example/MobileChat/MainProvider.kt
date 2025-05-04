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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

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
                    removeMyselfFromOthersFriends()
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

    //FRIENDS
    fun sendFriendRequest(receiverId: String) {
        val senderId = Firebase.auth.currentUser?.uid ?: return

        val senderRef = db.collection("users").document(senderId)
        val receiverRef = db.collection("users").document(receiverId)

        senderRef.update("invited_friends", FieldValue.arrayUnion(receiverId))
            .addOnSuccessListener {
                Log.d("FriendRequest", "Added $receiverId to invited_friends")
            }
            .addOnFailureListener {
                Log.e("FriendRequest", "Error adding to invited_friends", it)
            }

        receiverRef.update("friend_requests", FieldValue.arrayUnion(senderId))
            .addOnSuccessListener {
                Log.d("FriendRequest", "Added $senderId to $receiverId.friend_requests")
            }
            .addOnFailureListener {
                Log.e("FriendRequest", "Error adding to friend_requests", it)
            }
    }

    fun cancelSendFriendRequest(receiverId: String) {
        val senderId = Firebase.auth.currentUser?.uid ?: return

        val senderRef = db.collection("users").document(senderId)
        val receiverRef = db.collection("users").document(receiverId)

        senderRef.update("invited_friends", FieldValue.arrayRemove(receiverId))
            .addOnSuccessListener {
                Log.d("FriendRequest", "Removed $receiverId from invited_friends")
            }
            .addOnFailureListener {
                Log.e("FriendRequest", "Error adding to invited_friends", it)
            }

        receiverRef.update("friend_requests", FieldValue.arrayRemove(senderId))
            .addOnSuccessListener {
                Log.d("FriendRequest", "Removed $senderId from $receiverId.friend_requests")
            }
            .addOnFailureListener {
                Log.e("FriendRequest", "Error adding to friend_requests", it)
            }
    }

    fun cancelFriendRequest(senderId: String) {
        val receiverId = Firebase.auth.currentUser?.uid ?: return
        val receiverRef = db.collection("users").document(receiverId)
        val senderRef = db.collection("users").document(senderId)

        // Usuń z friend_requests i invited_friends
        receiverRef.update("friend_requests", FieldValue.arrayRemove(senderId))
            .addOnSuccessListener {
                Log.d(
                    "Accept",
                    "Removed $senderId from receiver's friend_requests"
                )
            }

        senderRef.update("invited_friends", FieldValue.arrayRemove(receiverId))
            .addOnSuccessListener {
                Log.d(
                    "Accept",
                    "Removed $receiverId from sender's invited_friends"
                )
            }
    }

    fun acceptFriendRequest(senderId: String) {
        val receiverId = Firebase.auth.currentUser?.uid ?: return
        val receiverRef = db.collection("users").document(receiverId)
        val senderRef = db.collection("users").document(senderId)

        // Usuń z friend_requests i invited_friends
        receiverRef.update("friend_requests", FieldValue.arrayRemove(senderId))
            .addOnSuccessListener { Log.d("Accept", "Removed $senderId from receiver's friend_requests") }

        senderRef.update("invited_friends", FieldValue.arrayRemove(receiverId))
            .addOnSuccessListener { Log.d("Accept", "Removed $receiverId from sender's invited_friends") }

        // Dodaj do friends
        receiverRef.update("friends", FieldValue.arrayUnion(senderId))
            .addOnSuccessListener { Log.d("Accept", "Added $senderId to receiver's friends") }

        senderRef.update("friends", FieldValue.arrayUnion(receiverId))
            .addOnSuccessListener { Log.d("Accept", "Added $receiverId to sender's friends") }
    }

    suspend fun getFriendRequests(): List<String> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.get("friend_requests") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("getFriendRequests", "Error fetching friend_requests", e)
            emptyList()
        }
    }

    private fun removeMyselfFromOthersFriends() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        // Usuń siebie z friends innych użytkowników
        userRef.get().addOnSuccessListener { document ->
            val friends = document.get("friends") as? List<String> ?: emptyList()
            friends.forEach { friendId ->
                db.collection("users").document(friendId)
                    .update("friends", FieldValue.arrayRemove(userId))
                    .addOnSuccessListener { Log.d("AccountDeletion", "Removed from $friendId's friends") }
                    .addOnFailureListener { Log.e("AccountDeletion", "Failed for $friendId", it) }
            }
        }

        // Usuń siebie z friend_requests innych użytkowników i wyczyść invited_friends
        userRef.get().addOnSuccessListener { document ->
            val invitedFriends = document.get("invited_friends") as? List<String> ?: emptyList()
            invitedFriends.forEach { receiverId ->
                db.collection("users").document(receiverId)
                    .update("friend_requests", FieldValue.arrayRemove(userId))
                    .addOnSuccessListener { Log.d("AccountDeletion", "Removed from $receiverId's friend_requests") }
                    .addOnFailureListener { Log.e("AccountDeletion", "Failed to update $receiverId", it) }
            }

            userRef.update("invited_friends", emptyList<String>())
                .addOnSuccessListener { Log.d("AccountDeletion", "Cleared invited_friends") }
                .addOnFailureListener { Log.e("AccountDeletion", "Failed to clear invited_friends", it) }
        }
    }


    suspend fun getInvitedFriends(): List<String> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptyList()

        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            snapshot.get("invited_friends") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("getInvitedFriends", "Error fetching invited_friends", e)
            emptyList()
        }
    }

    //Data to screens
    suspend fun getFriends(): List<String> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").document(uid).get().await()
        return snapshot.get("friends") as? List<String> ?: emptyList()
    }

    fun removeFriend(friendId: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return

        val userRef = db.collection("users").document(userId)
        val friendRef = db.collection("users").document(friendId)

        // Usuń siebie z jego friends i jego z siebie
        userRef.update("friends", FieldValue.arrayRemove(friendId))
            .addOnSuccessListener { Log.d("Friends", "Removed $friendId from user's friends") }
            .addOnFailureListener { Log.e("Friends", "Failed to update user", it) }

        friendRef.update("friends", FieldValue.arrayRemove(userId))
            .addOnSuccessListener { Log.d("Friends", "Removed $userId from friend's friends") }
            .addOnFailureListener { Log.e("Friends", "Failed to update friend", it) }
    }

    suspend fun getUserName(userId: String): String {
        val userRef = db.collection("users").document(userId)
        val snapshot = userRef.get().await()

        val name = snapshot.getString("name")?.trim()
        return if (!name.isNullOrEmpty()) {
            name
        } else {
            snapshot.getString("email") ?: "Nieznany użytkownik"
        }
    }

    suspend fun getFilteredInviteCandidates(query: String): List<String> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()

        val currentUserSnapshot = db.collection("users").document(currentUserId).get().await()

        val invitedFriends = currentUserSnapshot.get("invited_friends") as? List<String> ?: emptyList()
        val friends = currentUserSnapshot.get("friends") as? List<String> ?: emptyList()
        val friendRequests = currentUserSnapshot.get("friend_requests") as? List<String> ?: emptyList()

        val excludedIds = invitedFriends + friends + friendRequests + listOf(currentUserId)

        val allUsersSnapshot = db.collection("users").get().await()

        return allUsersSnapshot.documents
            .filter { it.id !in excludedIds }
            .mapNotNull { doc ->
                val id = doc.id
                val name = getUserName(id)
                if (name.contains(query, ignoreCase = true)) name else null
            }
    }
}
