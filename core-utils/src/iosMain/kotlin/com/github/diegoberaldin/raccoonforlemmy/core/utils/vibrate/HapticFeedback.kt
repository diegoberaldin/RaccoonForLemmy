package com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate

import org.koin.dsl.module
import platform.UIKit.UIImpactFeedbackGenerator

class DefaultHapticFeedback() : HapticFeedback {
    override fun vibrate() {
        UIImpactFeedbackGenerator().apply {
            prepare()
            impactOccurred()
        }
    }
}

actual val hapticFeedbackModule = module {
    single<HapticFeedback> {
        DefaultHapticFeedback()
    }
}
