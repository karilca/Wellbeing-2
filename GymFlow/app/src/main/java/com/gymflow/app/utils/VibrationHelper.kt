package com.gymflow.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationHelper(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrateShort() {
        vibrate(100)
    }

    fun vibrateMedium() {
        vibrate(300)
    }

    fun vibrateLong() {
        vibrate(600)
    }

    fun vibrateError() {
        val pattern = longArrayOf(0, 200, 100, 200)
        vibratePattern(pattern)
    }

    fun vibrateSuccess() {
        val pattern = longArrayOf(0, 100, 50, 100, 50, 200)
        vibratePattern(pattern)
    }

    fun vibrateWarning() {
        val pattern = longArrayOf(0, 150, 100, 150)
        vibratePattern(pattern)
    }

    fun vibrateRepComplete() {
        vibrate(50)
    }

    fun vibrateSetComplete() {
        val pattern = longArrayOf(0, 100, 100, 100, 100, 300)
        vibratePattern(pattern)
    }

    fun vibrateWorkoutComplete() {
        val pattern = longArrayOf(0, 200, 100, 200, 100, 200, 100, 500)
        vibratePattern(pattern)
    }

    private fun vibrate(milliseconds: Long) {
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun cancel() {
        vibrator.cancel()
    }
}