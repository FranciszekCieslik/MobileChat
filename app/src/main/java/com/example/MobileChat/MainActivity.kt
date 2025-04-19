@file:Suppress("DEPRECATION")

package com.example.MobileChat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import authentication.RegisterViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import database.FirestoreDatabaseProvider

class MainActivity : ComponentActivity() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var navController: NavHostController
    private lateinit var db: FirebaseFirestore
    private lateinit var dbProvider: FirestoreDatabaseProvider
    private lateinit var auth : FirebaseAuth

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
            App(navController, viewModel, dbProvider) // Pass NavController to the main component
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
    }

}

@Composable
fun App(navController: NavHostController, viewModel: RegisterViewModel,dbProvider: FirestoreDatabaseProvider) {
    NavGraph(navController, viewModel, dbProvider)
}
