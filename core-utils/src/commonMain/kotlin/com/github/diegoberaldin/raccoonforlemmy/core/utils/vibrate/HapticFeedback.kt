package com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate

import org.koin.core.module.Module

interface HapticFeedback {
    fun vibrate()
}

expect val hapticFeedbackModule: Module
