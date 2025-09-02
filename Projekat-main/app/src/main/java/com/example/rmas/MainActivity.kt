package com.example.rmas

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.rmas.app.App
import com.example.rmas.ui.theme.RMASTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RMASTheme(dynamicColor = false) {
                val navController = rememberNavController()
                App(navController)
            }
        }
    }
}
