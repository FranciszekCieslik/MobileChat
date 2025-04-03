package database
import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreDatabaseProvider {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun download() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()) {
                  val friends = document.get("friends")
                   Log.w(TAG, "Firebase DB connected")
                   if(friends != null){
                       Log.i(TAG, "You have friends")
                   }
              }
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase DB download failed")
            }
    }

    fun addUserToFirestore() {
        val user = auth.currentUser
        user?.let {
            val userMap = hashMapOf(
                "email" to user.email,
                "name" to (user.displayName ?: "Unknown"),
                "friends" to emptyList<String>()
            )

            db.collection("users").document(user.uid)
                .set(userMap)
                .addOnSuccessListener {
                    Log.d(TAG, "User added to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding user to Firestore", e)
                }

        }
    }


}