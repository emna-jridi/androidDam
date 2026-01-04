package tn.esprit.dam.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Haptic Feedback Utility
 * Provides tactile feedback for user interactions
 */
class HapticFeedback(private val context: Context) {
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    /**
     * Light tap - for button presses
     */
    fun lightTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }
    
    /**
     * Medium tap - for selections or confirmations
     */
    fun mediumTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }
    
    /**
     * Heavy tap - for errors or important actions
     */
    fun heavyTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
    
    /**
     * Success pattern - double tap
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 30, 50, 30)
            val amplitudes = intArrayOf(0, 100, 0, 100)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 50, 30), -1)
        }
    }
    
    /**
     * Error pattern - long buzz
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }
}

/**
 * Composable helper to remember HapticFeedback instance
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember { HapticFeedback(context) }
}
