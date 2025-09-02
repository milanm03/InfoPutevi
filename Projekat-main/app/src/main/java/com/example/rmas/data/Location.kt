package com.example.rmas.data

import com.google.firebase.Timestamp
import java.util.Date

data class Location(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var description: String = "",
    var type: String = "",
    var image: String = "",
    var date: Timestamp = Timestamp(Date())
) {
    fun doesMatchSearchQuery(query: String): Boolean {
        val regex = query.split(" ")
            .joinToString(".*") {
                Regex.escape(it)
            }.toRegex(RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(description) || regex.containsMatchIn(title)
    }
}