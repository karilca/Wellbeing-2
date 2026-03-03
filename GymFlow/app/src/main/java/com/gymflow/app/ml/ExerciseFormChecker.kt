package com.gymflow.app.ml

import com.gymflow.app.domain.model.FormStatus
import com.gymflow.app.domain.model.PoseLandmark
import com.gymflow.app.domain.model.PoseResult
import kotlin.math.abs
import kotlin.math.atan2

class ExerciseFormChecker {

    fun checkForm(
        landmarks: List<PoseLandmark>,
        exerciseName: String
    ): PoseResult {
        return when (exerciseName) {
            "Cucanj" -> checkSquatForm(landmarks)
            "Bench Press" -> checkBenchPressForm(landmarks)
            "Deadlift" -> checkDeadliftForm(landmarks)
            "Sklekovi" -> checkPushUpForm(landmarks)
            "Pull-up" -> checkPullUpForm(landmarks)
            else -> checkGeneralForm(landmarks)
        }
    }

    private fun checkSquatForm(landmarks: List<PoseLandmark>): PoseResult {
        val feedback = mutableListOf<String>()
        var formScore = 1.0f

        val leftHip = landmarks.getOrNull(PoseLandmark.LEFT_HIP)
        val rightHip = landmarks.getOrNull(PoseLandmark.RIGHT_HIP)
        val leftKnee = landmarks.getOrNull(PoseLandmark.LEFT_KNEE)
        val rightKnee = landmarks.getOrNull(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = landmarks.getOrNull(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = landmarks.getOrNull(PoseLandmark.RIGHT_ANKLE)
        val leftShoulder = landmarks.getOrNull(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = landmarks.getOrNull(PoseLandmark.RIGHT_SHOULDER)

        if (leftKnee != null && leftAnkle != null && leftHip != null) {
            val kneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
            if (kneeAngle < 70f) {
                feedback.add("Odlicno - duboki cucanj")
            } else if (kneeAngle > 120f) {
                feedback.add("Spustite se dublje")
                formScore -= 0.2f
            }
        }

        if (leftKnee != null && leftAnkle != null) {
            if (leftKnee.x > leftAnkle.x + 50) {
                feedback.add("Koljena ne smiju prelaziti prste")
                formScore -= 0.3f
            }
        }

        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            val shoulderMidX = (leftShoulder.x + rightShoulder.x) / 2
            val hipMidX = (leftHip.x + rightHip.x) / 2
            if (abs(shoulderMidX - hipMidX) > 80) {
                feedback.add("Drzite leda ravno")
                formScore -= 0.25f
            }
        }

        if (feedback.isEmpty()) {
            feedback.add("Odlicna forma - nastavite")
        }

        return PoseResult(
            landmarks = landmarks,
            formScore = formScore.coerceIn(0f, 1f),
            feedback = feedback,
            exerciseName = "Cucanj",
            isCorrectForm = formScore >= 0.8f
        )
    }

    private fun checkBenchPressForm(landmarks: List<PoseLandmark>): PoseResult {
        val feedback = mutableListOf<String>()
        var formScore = 1.0f

        val leftShoulder = landmarks.getOrNull(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = landmarks.getOrNull(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = landmarks.getOrNull(PoseLandmark.LEFT_ELBOW)
        val rightElbow = landmarks.getOrNull(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = landmarks.getOrNull(PoseLandmark.LEFT_WRIST)
        val rightWrist = landmarks.getOrNull(PoseLandmark.RIGHT_WRIST)

        if (leftElbow != null && leftShoulder != null && leftWrist != null) {
            val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
            if (elbowAngle < 70f) {
                feedback.add("Podignite sipku gore")
                formScore -= 0.2f
            } else if (elbowAngle > 160f) {
                feedback.add("Spustite sipku do prsa")
                formScore -= 0.2f
            }
        }

        if (leftShoulder != null && rightShoulder != null) {
            val shoulderDiff = abs(leftShoulder.y - rightShoulder.y)
            if (shoulderDiff > 30) {
                feedback.add("Poravnajte ramena")
                formScore -= 0.3f
            }
        }

        if (feedback.isEmpty()) {
            feedback.add("Odlicna forma - nastavite")
        }

        return PoseResult(
            landmarks = landmarks,
            formScore = formScore.coerceIn(0f, 1f),
            feedback = feedback,
            exerciseName = "Bench Press",
            isCorrectForm = formScore >= 0.8f
        )
    }

    private fun checkDeadliftForm(landmarks: List<PoseLandmark>): PoseResult {
        val feedback = mutableListOf<String>()
        var formScore = 1.0f

        val leftShoulder = landmarks.getOrNull(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = landmarks.getOrNull(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = landmarks.getOrNull(PoseLandmark.LEFT_HIP)
        val rightHip = landmarks.getOrNull(PoseLandmark.RIGHT_HIP)
        val leftKnee = landmarks.getOrNull(PoseLandmark.LEFT_KNEE)

        if (leftShoulder != null && leftHip != null && leftKnee != null) {
            val backAngle = calculateAngle(leftShoulder, leftHip, leftKnee)
            if (backAngle < 150f) {
                feedback.add("Drzite leda ravno")
                formScore -= 0.4f
            }
        }

        if (leftShoulder != null && rightShoulder != null) {
            val shoulderDiff = abs(leftShoulder.y - rightShoulder.y)
            if (shoulderDiff > 40) {
                feedback.add("Podignite ramena")
                formScore -= 0.3f
            }
        }

        if (feedback.isEmpty()) {
            feedback.add("Odlicna forma - nastavite")
        }

        return PoseResult(
            landmarks = landmarks,
            formScore = formScore.coerceIn(0f, 1f),
            feedback = feedback,
            exerciseName = "Deadlift",
            isCorrectForm = formScore >= 0.8f
        )
    }

    private fun checkPushUpForm(landmarks: List<PoseLandmark>): PoseResult {
        val feedback = mutableListOf<String>()
        var formScore = 1.0f

        val leftShoulder = landmarks.getOrNull(PoseLandmark.LEFT_SHOULDER)
        val leftHip = landmarks.getOrNull(PoseLandmark.LEFT_HIP)
        val leftAnkle = landmarks.getOrNull(PoseLandmark.LEFT_ANKLE)
        val leftElbow = landmarks.getOrNull(PoseLandmark.LEFT_ELBOW)
        val leftWrist = landmarks.getOrNull(PoseLandmark.LEFT_WRIST)

        if (leftShoulder != null && leftHip != null && leftAnkle != null) {
            val bodyAngle = calculateAngle(leftShoulder, leftHip, leftAnkle)
            if (abs(bodyAngle - 180f) > 20f) {
                feedback.add("Tijelo drzite ravno poput daske")
                formScore -= 0.4f
            }
        }

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
            if (elbowAngle < 60f) {
                feedback.add("Podignite se gore")
                formScore -= 0.2f
            }
        }

        if (feedback.isEmpty()) {
            feedback.add("Odlicna forma - nastavite")
        }

        return PoseResult(
            landmarks = landmarks,
            formScore = formScore.coerceIn(0f, 1f),
            feedback = feedback,
            exerciseName = "Sklekovi",
            isCorrectForm = formScore >= 0.8f
        )
    }

    private fun checkPullUpForm(landmarks: List<PoseLandmark>): PoseResult {
        val feedback = mutableListOf<String>()
        var formScore = 1.0f

        val leftShoulder = landmarks.getOrNull(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = landmarks.getOrNull(PoseLandmark.LEFT_ELBOW)
        val leftWrist = landmarks.getOrNull(PoseLandmark.LEFT_WRIST)
        val leftHip = landmarks.getOrNull(PoseLandmark.LEFT_HIP)

        if (leftShoulder != null && leftElbow != null && leftWrist != null) {
            val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
            if (elbowAngle > 150f) {
                feedback.add("Povucite se gore")
                formScore -= 0.3f
            }
        }

        if (leftShoulder != null && leftHip != null) {
            val torsoAngle = abs(leftShoulder.x - leftHip.x)
            if (torsoAngle > 50) {
                feedback.add("Drzite tijelo stabilno")
                formScore -= 0.2f
            }
        }

        if (feedback.isEmpty()) {
            feedback.add("Odlicna forma - nastavite")
        }

        return PoseResult(
            landmarks = landmarks,
            formScore = formScore.coerceIn(0f, 1f),
            feedback = feedback,
            exerciseName = "Pull-up",
            isCorrectForm = formScore >= 0.8f
        )
    }

    private fun checkGeneralForm(landmarks: List<PoseLandmark>): PoseResult {
        return PoseResult(
            landmarks = landmarks,
            formScore = 0.8f,
            feedback = listOf("Tijelo je detektirano"),
            exerciseName = "Opca vjezba",
            isCorrectForm = true
        )
    }

    private fun calculateAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Float {
        val result = Math.toDegrees(
            atan2(
                (lastPoint.y - midPoint.y).toDouble(),
                (lastPoint.x - midPoint.x).toDouble()
            ) - atan2(
                (firstPoint.y - midPoint.y).toDouble(),
                (firstPoint.x - midPoint.x).toDouble()
            )
        ).toFloat()

        var angle = abs(result)
        if (angle > 180) {
            angle = 360 - angle
        }
        return angle
    }
}