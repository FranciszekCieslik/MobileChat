@file:Suppress("DEPRECATION")

package com.example.MobileChat

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import authentication.RegisterState
import authentication.RegisterViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import database.FirestoreDatabaseProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var navController: NavHostController
    private lateinit var db: FirebaseFirestore
    private lateinit var dbProvider: FirestoreDatabaseProvider

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth : FirebaseAuth

    private val signInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        Firebase.auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    dbProvider.addUserToFirestore()
                                    dbProvider.download()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Log.w(TAG, "Firebase sign-in failed", task.exception)
                                }
                            }
                    } else {
                        Log.w(TAG, "No ID token!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "One Tap sign-in error: ${e.localizedMessage}")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init FirebaseApp connection
        FirebaseApp.initializeApp(this)

        // Init Database connection and provider
        db = Firebase.firestore
        dbProvider = FirestoreDatabaseProvider()
        auth = viewModel.auth
        setContent {
            navController = rememberNavController() // Initialize navController
            App(navController, viewModel) // Pass NavController to the main component
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }

}

@Composable
fun App(navController: NavHostController, viewModel: RegisterViewModel) {
    NavGraph(navController, viewModel)
}
