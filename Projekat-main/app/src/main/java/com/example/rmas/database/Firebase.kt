package com.example.rmas.database

import com.example.rmas.data.Like
import com.example.rmas.data.Location
import com.example.rmas.data.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

object Firebase {
    fun getLocations(listener: (List<Location>) -> Unit) {
        val db = Firebase.firestore
        var locations: MutableList<Location>
        db.collection("locations")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    locations = snap.toObjects(Location::class.java)
                    listener(locations)
                }
            }
    }

    fun getUser(uid: String, listener: (User?) -> Unit) {
        val db = Firebase.firestore
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener {
                if (it.exists())
                    listener(it.toObject(User::class.java))
            }
    }

    fun getLocation(uid: String, listener: (Location?) -> Unit) {
        val db = Firebase.firestore
        db.collection("locations")
            .document(uid)
            .get()
            .addOnSuccessListener {
                if (it.exists())
                    listener(it.toObject(Location::class.java))
            }
    }

    fun getAllUsers(listener: (List<User>) -> Unit) {
        val db = Firebase.firestore
        var users: MutableList<User>
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    users = snap.toObjects(User::class.java)
                    listener(users)
                }
            }
    }

    fun addLikeToDb(userId: String, locationId: String) {
        val db = Firebase.firestore
        val ref = db.collection("likes").document()
        val like = Like(id = ref.id, userId = userId, locationId = locationId)
        ref.set(like)
            .addOnSuccessListener {
                db.collection("users").document(userId)
                    .update("points", FieldValue.increment(1L))
            }
    }

    fun removeLikeFromDb(userId: String, locationId: String) {
        val db = Firebase.firestore
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .whereEqualTo("locationId", locationId)
            .get()
            .addOnSuccessListener {
                it.documents[0].reference.delete()
                    .addOnSuccessListener {
                        db.collection("users").document(userId)
                            .update("points", FieldValue.increment(-1L))
                    }
            }
    }

    fun didUserLike(userId: String, locationId: String, listener: (Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("likes")
            .whereEqualTo("userId", userId)
            .whereEqualTo("locationId", locationId)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    if (snap.isEmpty)
                        listener(false)
                    else
                        listener(true)
                }
            }
    }
}