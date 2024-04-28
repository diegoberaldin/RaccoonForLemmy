package com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class DefaultHapticFeedback(
    private val context: Context,
) : HapticFeedback {
    @SuppressLint("MissingPermission")
    override fun vibrate() {
        val vibrator = context.getSystemService(Vibrator::class.java)
        val effect = if (Build.VERSION.SDK_INT >= 29) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        } else {
            VibrationEffect.createOneShot(
                1L,
                VibrationEffect.DEFAULT_AMPLITUDE,
            )
        }
        vibrator.vibrate(effect)
    }
}
