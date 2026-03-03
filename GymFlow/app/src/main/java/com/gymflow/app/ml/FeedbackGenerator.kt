package com.gymflow.app.ml

import android.content.Context
import android.speech.tts.TextToSpeech
import com.gymflow.app.domain.model.FormStatus
import com.gymflow.app.domain.model.PoseResult
import java.util.Locale

class FeedbackGenerator(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var lastFeedbackTime = 0L
    private val feedbackCooldownMs = 3000L

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("hr", "HR"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.ENGLISH)
            }
            isInitialized = true
        }
    }

    fun generateFeedback(poseResult: PoseResult): String {
        val currentTime = System.currentTimeMillis()
        val primaryFeedback = poseResult.primaryFeedback

        if (primaryFeedback.isNotEmpty() &&
            currentTime - lastFeedbackTime > feedbackCooldownMs) {
            if (poseResult.formStatus != FormStatus.GOOD) {
                speak(primaryFeedback)
                lastFeedbackTime = currentTime
            }
        }

        return primaryFeedback
    }

    fun speakFeedback(text: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFeedbackTime > feedbackCooldownMs) {
            speak(text)
            lastFeedbackTime = currentTime
        }
    }

    private fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "gymflow_feedback"
            )
        }
    }

    fun getMotivationalMessage(formScore: Float): String {
        return when {
            formScore >= 0.9f -> "Izvrsno - savrsena forma"
            formScore >= 0.8f -> "Odlicno - nastavite ovako"
            formScore >= 0.7f -> "Dobro - malo popravite formu"
            formScore >= 0.5f -> "Pazite na formu"
            else -> "Zaustavite se i popravite formu"
        }
    }

    fun getExerciseTip(exerciseName: String): String {
        return when (exerciseName) {
            "Cucanj" -> "Drzite pete na podu i gledajte naprijed"
            "Bench Press" -> "Drzite lopatice skupljene i stopala na podu"
            "Deadlift" -> "Leda ravno i brada gore"
            "Sklekovi" -> "Tijelo ravno poput daske"
            "Pull-up" -> "Kontrolirano spustanje jednako je vazno"
            else -> "Fokusirajte se na pravilnu formu"
        }
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}