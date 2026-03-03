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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.app.domain.model.Exercise
import com.gymflow.app.domain.model.FormStatus
import com.gymflow.app.ui.theme.FormCorrect
import com.gymflow.app.ui.theme.FormError
import com.gymflow.app.ui.theme.FormWarning
import com.gymflow.app.ui.theme.GymGreen
import com.gymflow.app.ui.theme.SurfaceDark
import com.gymflow.app.ui.theme.TextPrimary
import com.gymflow.app.ui.theme.TextSecondary

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isActive: Boolean = false,
    onStartClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                GymGreen.copy(alpha = 0.15f)
            else SurfaceDark
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = GymGreen
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) GymGreen.copy(alpha = 0.3f)
                                else GymGreen.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GymGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = exercise.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) GymGreen else TextPrimary
                        )
                        Text(
                            text = exercise.muscleGroup.replace(",", ", "),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onStartClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) GymGreen
                            else GymGreen.copy(alpha = 0.2f)
                        )
                ) {
                    Icon(
                        imageVector = if (isActive)
                            Icons.Default.CheckCircle
                        else Icons.Default.PlayArrow,
                        contentDescription = "Pokreni",
                        tint = if (isActive) SurfaceDark else GymGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (exercise.formScore > 0f) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forma",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${exercise.formScorePercentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (exercise.formStatus) {
                            FormStatus.GOOD -> FormCorrect
                            FormStatus.WARNING -> FormWarning
                            FormStatus.BAD -> FormError
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SurfaceDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(exercise.formScore)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when (exercise.formStatus) {
                                    FormStatus.GOOD -> FormCorrect
                                    FormStatus.WARNING -> FormWarning
                                    FormStatus.BAD -> FormError
                                }
                            )
                    )
                }
            }

            if (exercise.sets > 0 || exercise.reps > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (exercise.sets > 0) {
                        ExerciseStat(label = "Serija", value = "${exercise.sets}")
                    }
                    if (exercise.reps > 0) {
                        ExerciseStat(label = "Ponavljanja", value = "${exercise.reps}")
                    }
                    if (exercise.weightKg > 0f) {
                        ExerciseStat(label = "Tezina", value = "${exercise.weightKg}kg")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseStat(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}