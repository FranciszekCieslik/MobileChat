package com.example.MobileChat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class MainActivity : ComponentActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var provider: MainProvider
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init FirebaseApp connection
        FirebaseApp.initializeApp(this)

        // Init Database connection and provider
        db = Firebase.firestore
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()  // ✅ Użycie getInstance()

        provider = MainProvider()
        setContent {
            navController = rememberNavController()
            App(navController, provider)
        }
    }
}

@Composable
fun App(navController: NavHostController, provider: MainProvider) {
    NavGraph(navController, provider)
}
