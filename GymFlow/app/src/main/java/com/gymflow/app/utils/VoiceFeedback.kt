package com.gymflow.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceFeedback(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var onSpeakComplete: (() -> Unit)? = null

    private val messageQueue = mutableListOf<String>()
    private var isSpeaking = false

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("hr", "HR"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                textToSpeech?.setLanguage(Locale.ENGLISH)
            }
            textToSpeech?.setSpeechRate(0.9f)
            textToSpeech?.setPitch(1.0f)
            isInitialized = true

            textToSpeech?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        onSpeakComplete?.invoke()
                        processQueue()
                    }

                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        processQueue()
                    }
                }
            )
        }
    }

    fun speak(text: String, priority: Boolean = false) {
        if (!isInitialized) return
        if (priority) {
            messageQueue.clear()
            textToSpeech?.stop()
            isSpeaking = false
        }
        if (!isSpeaking) {
            speakNow(text)
        } else {
            messageQueue.add(text)
        }
    }

    private fun speakNow(text: String) {
        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "gymflow_${System.currentTimeMillis()}"
        )
    }

    private fun processQueue() {
        if (messageQueue.isNotEmpty() && !isSpeaking) {
            val nextMessage = messageQueue.removeAt(0)
            speakNow(nextMessage)
        }
    }

    fun speakExerciseStart(exerciseName: String) {
        speak("Pocinjemo s vjezbon $exerciseName", priority = true)
    }

    fun speakFormFeedback(feedback: String) {
        speak(feedback)
    }

    fun speakMotivation(score: Float) {
        val message = when {
            score >= 0.9f -> "Izvrsno, savrsena forma"
            score >= 0.8f -> "Odlicno, nastavite"
            score >= 0.7f -> "Dobro, malo popravite formu"
            score >= 0.5f -> "Pazite na formu"
            else -> "Zaustavite se i popravite formu"
        }
        speak(message)
    }

    fun speakRemainingReps(reps: Int) {
        speak("Jos $reps ponavljanja")
    }

    fun speakSetComplete(setNumber: Int) {
        speak("Serija $setNumber zavrsena, odmor")
    }

    fun speakWorkoutComplete() {
        speak("Trening je zavrsen, odlican posao", priority = true)
    }

    fun stop() {
        textToSpeech?.stop()
        messageQueue.clear()
        isSpeaking = false
    }

    fun setOnSpeakCompleteListener(listener: () -> Unit) {
        onSpeakComplete = listener
    }

    fun release() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}