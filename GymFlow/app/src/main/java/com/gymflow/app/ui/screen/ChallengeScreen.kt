package com.gymflow.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gymflow.app.ui.theme.AccentOrange
import com.gymflow.app.ui.theme.BackgroundDark
import com.gymflow.app.ui.theme.GymGreen
import com.gymflow.app.ui.theme.SurfaceDark
import com.gymflow.app.ui.theme.TextPrimary
import com.gymflow.app.ui.theme.TextSecondary

data class ChallengeItem(
    val rank: Int,
    val name: String,
    val score: Int,
    val workouts: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun ChallengeScreen(navController: NavController) {

    val leaderboard = listOf(
        ChallengeItem(1, "Korisnik_A", 2450, 18),
        ChallengeItem(2, "Korisnik_B", 2210, 15),
        ChallengeItem(3, "Vi", 1980, 12, isCurrentUser = true),
        ChallengeItem(4, "Korisnik_C", 1750, 11),
        ChallengeItem(5, "Korisnik_D", 1620, 10)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Nazad",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Izazovi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GymGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "QR kod",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = GymGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Bluetooth",
                        color = GymGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tjedni izazov",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ostvarite 15 treninga ovog tjedna",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vas napredak: 12/15",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "3 dana preostalo",
                            fontSize = 12.sp,
                            color = AccentOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ljestvica",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaderboard) { item ->
                    LeaderboardCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(item: ChallengeItem) {
    val rankColor = when (item.rank) {
        1 -> Color(0xFFFFD600)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFFF6D00)
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCurrentUser)
                GymGreen.copy(alpha = 0.15f)
            else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "#${item.rank}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = rankColor,
                modifier = Modifier.size(36.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = if (item.isCurrentUser)
                        FontWeight.Bold else FontWeight.Normal,
                    color = if (item.isCurrentUser) GymGreen else TextPrimary
                )
                Text(
                    text = "${item.workouts} treninga",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = "${item.score}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isCurrentUser) GymGreen else TextPrimary
            )
        }
    }
}