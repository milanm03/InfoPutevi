package com.example.rmas.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.rmas.data.Location
import com.example.rmas.data.User
import com.example.rmas.database.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    locationId: String
) {
    var user by remember { mutableStateOf(User()) }
    var location by remember { mutableStateOf(Location()) }
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    var liked by remember { mutableStateOf(false) }
    Firebase.getLocation(locationId) {
        if (it != null) {
            location = it
            Firebase.getUser(location.userId) { us ->
                if (us != null) {
                    user = us
                }
            }
            Firebase.didUserLike(userId, location.id) { bool ->
                liked = bool
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            isSheetOpen.value = false
        },
        sheetState = sheetState,
    ) {
        LazyColumn {

            // 1. SLIKA (sada je na vrhu)
            // Uklonjen je horizontalni padding da bi slika zauzela celu širinu sheet-a
            item {
                Image(
                    painter = rememberAsyncImagePainter(model = location.image),
                    contentDescription = "Slika lokacije",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp), // Malo povećana visina za bolji vizuelni utisak
                    contentScale = ContentScale.Crop
                )
            }

            // 2. NAZIV i LIKE DUGME
            item {
                Row(
                    modifier = Modifier
                        // Dodat gornji padding da se odvoji od slike
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = location.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f, fill = false) // Sprečava da tekst gura ikonicu
                    )
                    if (location.userId != userId) {
                        IconToggleButton(
                            checked = liked,
                            onCheckedChange = {
                                if (liked) {
                                    Firebase.removeLikeFromDb(userId, location.id)
                                } else {
                                    Firebase.addLikeToDb(userId, location.id)
                                }
                                liked = !liked // Trenutna promena UI-a za bolji odziv
                            }
                        ) {
                            Icon(
                                tint = Color(0xffE91E63),
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = 1.3f
                                        scaleY = 1.3f
                                    },
                                imageVector = if (liked) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = "Like"
                            )
                        }
                    }
                }
            }


            item {
                Text(
                    text = location.description,
                    modifier = Modifier
                        .padding(top = 8.dp, start = 20.dp, end = 20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify
                )
            }


            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = rememberAsyncImagePainter(model = user.image),
                        contentDescription = "Profilna slika korisnika",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(30.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(user.username, style = MaterialTheme.typography.titleMedium)


                    Spacer(modifier = Modifier.weight(1f))


                    Text(
                        text = convertTimestampToDate(location.date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertTimestampToDate(timestamp: Timestamp): String {
    val date = timestamp
        .toDate()
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'u' HH:mm")

    return date.format(formatter)
}