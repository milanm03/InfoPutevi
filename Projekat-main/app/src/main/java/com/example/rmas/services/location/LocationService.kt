package com.example.rmas.services.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.rmas.MainActivity
import com.example.rmas.R
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.cos

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private val notifiedLocations = mutableSetOf<String>()
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "Location",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                start()
            }

            ACTION_START_NEARBY -> {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
                start(true)
            }

            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    private fun start(nearbyService: Boolean = false) {
        locationClient
            .getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                UserLocation.location.value = location
                if (nearbyService) {
                    sendNearbyLocationsNotification(location.latitude, location.longitude)
                }
            }
            .launchIn(serviceScope)
    }

    private fun createNotification(): Notification {
        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "location")
            .setContentTitle("Praćenje lokacije...")
            .setContentText("Servis u pozadini prati vašu lokaciju.")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun sendNearbyLocationsNotification(lat: Double, lng: Double) {
        val radiusInKm = 0.1
        val earthRadiusKm = 6371.0

        val latDelta = Math.toDegrees(radiusInKm / earthRadiusKm)
        val lngDelta = Math.toDegrees(radiusInKm / (earthRadiusKm * cos(Math.toRadians(lat))))

        val minLat = lat - latDelta
        val maxLat = lat + latDelta
        val minLng = lng - lngDelta
        val maxLng = lng + lngDelta

        val db = Firebase.firestore
        db.collection("locations")
            .whereGreaterThanOrEqualTo("latitude", minLat)
            .whereLessThanOrEqualTo("latitude", maxLat)
            .whereGreaterThanOrEqualTo("longitude", minLng)
            .whereLessThanOrEqualTo("longitude", maxLng)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    for (doc in snap) {
                        if (!notifiedLocations.contains(doc.id)) {
                            sendNotification(doc.get("title").toString())
                            notifiedLocations.add(doc.id)
                        }
                    }
                }
            }
    }

    private fun sendNotification(title: String) {
        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setContentTitle("Obelezena lokacija u blizini")
            .setContentText("Nalazite se u blizini obelezene lokacije: ${title}!")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NEARBY_NOTIFICATION_ID, notification)
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_START_NEARBY = "ACTION_START_NEARBY"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1
        const val NEARBY_NOTIFICATION_ID = 2
        const val NOTIFICATION_CHANNEL = "location"
    }
}