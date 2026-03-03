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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gymflow.app.ui.component.MuscleHeatmap
import com.gymflow.app.ui.theme.AccentOrange
import com.gymflow.app.ui.theme.BackgroundDark
import com.gymflow.app.ui.theme.GymGreen
import com.gymflow.app.ui.theme.SurfaceDark
import com.gymflow.app.ui.theme.TextPrimary
import com.gymflow.app.ui.theme.TextSecondary

@Composable
fun ProgressScreen(navController: NavController) {

    val weeklyStats = listOf(
        "Pon" to 3,
        "Uto" to 5,
        "Sri" to 2,
        "Cet" to 7,
        "Pet" to 4,
        "Sub" to 6,
        "Ned" to 1
    )

    val activeMuscles = mapOf(
        "chest" to 0.8f,
        "back" to 0.6f,
        "shoulders" to 0.4f,
        "biceps" to 0.5f,
        "triceps" to 0.7f,
        "quadriceps" to 0.9f,
        "hamstrings" to 0.3f,
        "glutes" to 0.6f,
        "calves" to 0.2f,
        "core" to 0.5f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = "Napredak",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        value = "12",
                        label = "Treninga",
                        color = GymGreen
                    )
                    StatItem(
                        value = "87%",
                        label = "Avg forma",
                        color = GymGreen
                    )
                    StatItem(
                        value = "3840",
                        label = "Kalorija",
                        color = AccentOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tjedna aktivnost",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyStats.forEach { (day, value) ->
                        WeeklyBarItem(day = day, value = value, maxValue = 7)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Heatmap misica",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Aktivnost misica ovog tjedna",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            MuscleHeatmap(
                activeMuscles = activeMuscles,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Rezultati forme po vjezbi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("Cucanj", 0.92f, GymGreen),
                Triple("Bench Press", 0.78f, GymGreen),
                Triple("Deadlift", 0.65f, AccentOrange),
                Triple("Sklekovi", 0.88f, GymGreen),
                Triple("Pull-up", 0.71f, AccentOrange)
            ).forEach { (name, score, color) ->
                FormScoreRow(
                    exerciseName = name,
                    score = score,
                    color = color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WeeklyBarItem(
    day: String,
    value: Int,
    maxValue: Int
) {
    val heightFraction = value.toFloat() / maxValue.toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .height((80 * heightFraction).dp)
                .padding(horizontal = 4.dp)
                .background(
                    color = if (value > 0) GymGreen else SurfaceDark,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun FormScoreRow(
    exerciseName: String,
    score: Float,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exerciseName,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(score * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}