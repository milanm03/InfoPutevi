package com.example.rmas

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Runtime check: true for debug builds
        val isDebuggable =
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val appCheck = FirebaseAppCheck.getInstance()
        if (isDebuggable) {
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d("AppCheck", "Installed Debug provider")
        } else {
            appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            Log.d("AppCheck", "Installed Play Integrity provider")
        }

        // Optional: pull a token once so you see a log quickly
        appCheck.getToken(false)
            .addOnSuccessListener { t ->
                Log.d("AppCheck", "Token prefix: ${t.token.take(16)}…")
            }
            .addOnFailureListener { e ->
                Log.e("AppCheck", "Failed to get token", e)
            }
    }
}