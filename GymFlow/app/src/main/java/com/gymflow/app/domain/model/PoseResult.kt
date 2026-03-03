package com.gymflow.app.domain.model

data class PoseResult(
    val landmarks: List<PoseLandmark> = emptyList(),
    val formScore: Float = 0f,
    val feedback: List<String> = emptyList(),
    val exerciseName: String = "",
    val isCorrectForm: Boolean = false,
    val activeJoints: List<String> = emptyList(),
    val angles: Map<String, Float> = emptyMap()
) {
    val formStatus: FormStatus
        get() = when {
            formScore >= 0.8f -> FormStatus.GOOD
            formScore >= 0.5f -> FormStatus.WARNING
            else -> FormStatus.BAD
        }

    val primaryFeedback: String
        get() = feedback.firstOrNull() ?: ""
}

data class PoseLandmark(
    val type: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val inFrameLikelihood: Float = 0f
) {
    companion object {
        const val NOSE = 0
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
    }
}

data class JointAngle(
    val jointName: String = "",
    val angle: Float = 0f,
    val minAngle: Float = 0f,
    val maxAngle: Float = 0f
) {
    val isInRange: Boolean
        get() = angle in minAngle..maxAngle
}