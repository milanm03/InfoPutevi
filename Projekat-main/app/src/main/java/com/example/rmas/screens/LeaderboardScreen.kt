package com.example.rmas.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rmas.data.User
import com.example.rmas.database.Firebase

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaderboardScreen() {
    var users by remember { mutableStateOf(emptyList<User>()) }


    LaunchedEffect(Unit) {
        Firebase.getAllUsers { fetchedUsers ->
            users = fetchedUsers.sortedByDescending { it.points }
        }
    }

    val topThree = users.take(3)
    val restOfUsers = if (users.size > 3) users.subList(3, users.size) else emptyList()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                if (topThree.isNotEmpty()) {
                    Text(
                        text = "Najpouzdaniji reporteri",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TopThreePodium(users = topThree)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Ostatak liste
            if (restOfUsers.isNotEmpty()) {
                stickyHeader {
                    ListHeader()
                }
                itemsIndexed(restOfUsers, key = { _, user -> user.id }) { index, user ->
                    // Index je od 0, a pošto prikazujemo od 4. mesta, dodajemo 4.
                    LeaderboardListItem(user = user, rank = index + 4)
                }
            }
        }
    }
}

@Composable
fun TopThreePodium(users: List<User>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2. Mesto
        if (users.size >= 2) {
            PodiumItem(
                user = users[1],
                rank = 2,
                color = Color(0xFFC0C0C0), // Silver
                modifier = Modifier.weight(1f)
            )
        }
        // 1. Mesto
        if (users.isNotEmpty()) {
            PodiumItem(
                user = users[0],
                rank = 1,
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFFFFD700), // Gold
                modifier = Modifier.weight(1f).padding(bottom = 24.dp) // Izdignut
            )
        }
        // 3. Mesto
        if (users.size >= 3) {
            PodiumItem(
                user = users[2],
                rank = 3,
                color = Color(0xFFCD7F32), // Bronze
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PodiumItem(
    user: User,
    rank: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Prvo mesto",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        Image(
            painter = rememberAsyncImagePainter(user.image),
            contentDescription = user.username,
            modifier = Modifier
                .size(if (rank == 1) 80.dp else 60.dp)
                .clip(CircleShape)
                .border(2.dp, color, CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(
            text = user.username,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${user.points} poena",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rank",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = "Korisnik",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Poeni",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LeaderboardListItem(user: User, rank: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(50.dp)
            )
            Image(
                painter = rememberAsyncImagePainter(user.image),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${user.points}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}