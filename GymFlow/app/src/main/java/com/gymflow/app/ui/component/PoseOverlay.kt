package com.gymflow.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.gymflow.app.domain.model.FormStatus
import com.gymflow.app.domain.model.PoseLandmark
import com.gymflow.app.domain.model.PoseResult
import com.gymflow.app.ui.theme.FormCorrect
import com.gymflow.app.ui.theme.FormError
import com.gymflow.app.ui.theme.FormWarning

@Composable
fun PoseOverlay(
    poseResult: PoseResult,
    modifier: Modifier = Modifier
) {
    val pointColor = when (poseResult.formStatus) {
        FormStatus.GOOD -> FormCorrect
        FormStatus.WARNING -> FormWarning
        FormStatus.BAD -> FormError
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val landmarks = poseResult.landmarks

        if (landmarks.isEmpty()) return@Canvas

        fun getLandmarkOffset(type: Int): Offset? {
            val landmark = landmarks.getOrNull(type) ?: return null
            return Offset(
                x = landmark.x * canvasWidth,
                y = landmark.y * canvasHeight
            )
        }

        fun drawConnection(startType: Int, endType: Int, color: Color) {
            val start = getLandmarkOffset(startType) ?: return
            val end = getLandmarkOffset(endType) ?: return
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        val lineColor = pointColor.copy(alpha = 0.8f)

        drawConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, lineColor)
        drawConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, lineColor)
        drawConnection(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, lineColor)
        drawConnection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, lineColor)
        drawConnection(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, lineColor)
        drawConnection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, lineColor)
        drawConnection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, lineColor)
        drawConnection(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, lineColor)
        drawConnection(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, lineColor)
        drawConnection(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, lineColor)
        drawConnection(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, lineColor)
        drawConnection(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, lineColor)

        landmarks.forEach { landmark ->
            val offset = Offset(
                x = landmark.x * canvasWidth,
                y = landmark.y * canvasHeight
            )
            if (landmark.inFrameLikelihood > 0.5f) {
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = offset
                )
                drawCircle(
                    color = pointColor,
                    radius = 5f,
                    center = offset
                )
            }
        }
    }
}