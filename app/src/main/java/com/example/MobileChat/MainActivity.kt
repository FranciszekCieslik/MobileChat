@file:Suppress("DEPRECATION")

package com.example.MobileChat

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.google.android.gms.common.api.ApiException
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import authentication.RegisterViewModel
import com.example.MobileChat.ui.theme.MobileChatTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import database.FirestoreDatabaseProvider

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var navController: NavHostController
    private  lateinit var db:FirebaseFirestore
    private lateinit var dbProvider:FirestoreDatabaseProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Init FirebaseApp connection
        FirebaseApp.initializeApp(this)

        // Init Database connection and provider
        db = Firebase.firestore
        dbProvider = FirestoreDatabaseProvider()

        // BEGIN AUTHENTICATION BY GOOGLE
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        // END AUTHENTICATION BY GOOGLE
        setContent {
            navController = rememberNavController() // Inicjalizacja navController
            App(navController,  viewModel) // Przekazanie NavController do głównego komponentu
        }
    }

    // BEGIN AUTHENTICATION BY GOOGLE
    private val reqONEtap = 2
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == reqONEtap) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Firebase.auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                dbProvider.addUserToFirestore()
                                dbProvider.download()
//                                Log.d(TAG, "One Tap sign-in successful")
                                // Zalogowano - nawiguj do głównego ekranu
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
//                                Log.w(TAG, "Firebase sign-in failed", task.exception)
                            }
                        }
                } else {
//                    Log.w(TAG, "No ID token!")
                }
            } catch (e: ApiException) {
//                Log.e(TAG, "One Tap sign-in error: ${e.statusCode}")
            }
        }
    }

    fun startSignIn() {
        if (!::oneTapClient.isInitialized || !::signInRequest.isInitialized) {
            // Możesz dodać logowanie błędu
            return
        }
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                startIntentSenderForResult(
                    result.pendingIntent.intentSender,
                    reqONEtap,
                    null,
                    0,
                    0,
                    0
                )
            }
            .addOnFailureListener { e ->
//                Log.e(TAG, "One Tap sign-in failed: ${e.localizedMessage}")
            }
    }

    // END AUTHENTICATION BY GOOGLE

}

@Composable
fun App(navController: NavHostController, viewModel: RegisterViewModel) {
    NavGraph(navController, viewModel)
}