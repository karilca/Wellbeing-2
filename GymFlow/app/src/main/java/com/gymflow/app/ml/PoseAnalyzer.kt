package com.gymflow.app.ml

import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.gymflow.app.domain.model.PoseLandmark
import com.gymflow.app.domain.model.PoseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PoseAnalyzer(
    private val context: Context,
    private val exerciseFormChecker: ExerciseFormChecker
) {

    private val _poseResult = MutableStateFlow<PoseResult?>(null)
    val poseResult: StateFlow<PoseResult?> = _poseResult.asStateFlow()

    private var currentExercise: String = "Cucanj"

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    fun setCurrentExercise(exerciseName: String) {
        currentExercise = exerciseName
    }

    @androidx.camera.core.ExperimentalGetImage
    fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val landmarks = pose.allPoseLandmarks.map { landmark ->
                    PoseLandmark(
                        type = landmark.landmarkType,
                        x = landmark.position.x,
                        y = landmark.position.y,
                        z = landmark.position3D.z,
                        inFrameLikelihood = landmark.inFrameLikelihood
                    )
                }

                if (landmarks.isNotEmpty()) {
                    val result = exerciseFormChecker.checkForm(
                        landmarks = landmarks,
                        exerciseName = currentExercise
                    )
                    _poseResult.value = result
                }
            }
            .addOnFailureListener {
                _poseResult.value = PoseResult()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun calculateAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Float {
        val result = Math.toDegrees(
            (Math.atan2(
                (lastPoint.y - midPoint.y).toDouble(),
                (lastPoint.x - midPoint.x).toDouble()
            ) - Math.atan2(
                (firstPoint.y - midPoint.y).toDouble(),
                (firstPoint.x - midPoint.x).toDouble()
            ))
        ).toFloat()

        var angle = Math.abs(result)
        if (angle > 180) {
            angle = 360 - angle
        }
        return angle
    }

    fun close() {
        poseDetector.close()
    }
}