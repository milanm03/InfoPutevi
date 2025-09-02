package com.example.rmas.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rmas.screens.HomeScreen
import com.example.rmas.screens.AddMarkerScreen
import com.example.rmas.screens.LoginScreen
import com.example.rmas.screens.LeaderboardScreen
import com.example.rmas.screens.LocationScreen
import com.example.rmas.screens.SingUpScreen
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(navController: NavHostController) {
    val startDestination: String = if (FirebaseAuth.getInstance().currentUser == null)
        "LoginScreen"
    else "HomeScreen"
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("LoginScreen") {
                LoginScreen(navController)
            }
            composable("SingUpScreen") {
                SingUpScreen(navController)
            }
            composable("HomeScreen") {
                HomeScreen(startDestination, navController)
            }
            composable("LeaderboardScreen") {
                LeaderboardScreen()
            }
            composable("AddMarkerScreen") {
                AddMarkerScreen(navController)
            }
            composable("LocationScreen") {
                LocationScreen()
            }
        }
    }
}

