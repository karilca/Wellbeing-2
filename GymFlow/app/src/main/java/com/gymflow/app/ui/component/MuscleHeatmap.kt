package com.gymflow.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.app.ui.theme.HeatmapHigh
import com.gymflow.app.ui.theme.HeatmapLow
import com.gymflow.app.ui.theme.HeatmapMax
import com.gymflow.app.ui.theme.HeatmapMedium
import com.gymflow.app.ui.theme.SurfaceDark
import com.gymflow.app.ui.theme.TextPrimary
import com.gymflow.app.ui.theme.TextSecondary

@Composable
fun MuscleHeatmap(
    activeMuscles: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val muscleGroups = listOf(
        "chest" to "Prsa",
        "back" to "Leda",
        "shoulders" to "Ramena",
        "biceps" to "Biceps",
        "triceps" to "Triceps",
        "core" to "Trbuh",
        "quadriceps" to "Kvadriceps",
        "hamstrings" to "Zadnja loza",
        "glutes" to "Gluteus",
        "calves" to "Listovi"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivnost misica",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                HeatmapLegend()
            }

            Spacer(modifier = Modifier.height(16.dp))

            muscleGroups.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (key, displayName) ->
                        val intensity = activeMuscles[key] ?: 0f
                        MuscleBar(
                            muscleName = displayName,
                            intensity = intensity,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MuscleBar(
    muscleName: String,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    val heatColor = getHeatmapColor(intensity)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = muscleName,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = "${(intensity * 100).toInt()}%",
                fontSize = 11.sp,
                color = heatColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceDark.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(intensity)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(heatColor)
            )
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Nisko",
            fontSize = 10.sp,
            color = TextSecondary
        )
        listOf(0.0f, 0.33f, 0.66f, 1.0f).forEach { value ->
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getHeatmapColor(value))
            )
        }
        Text(
            text = "Visoko",
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

fun getHeatmapColor(intensity: Float): Color {
    return when {
        intensity <= 0f -> HeatmapLow
        intensity <= 0.33f -> lerp(HeatmapLow, HeatmapMedium, intensity / 0.33f)
        intensity <= 0.66f -> lerp(HeatmapMedium, HeatmapHigh, (intensity - 0.33f) / 0.33f)
        else -> lerp(HeatmapHigh, HeatmapMax, (intensity - 0.66f) / 0.34f)
    }
}