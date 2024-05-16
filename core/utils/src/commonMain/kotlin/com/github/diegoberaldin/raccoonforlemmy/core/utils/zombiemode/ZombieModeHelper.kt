package com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ZombieModeHelper {
    val index: Flow<Int>

    fun start(
        initialValue: Int = 0,
        interval: Duration = 2.5.seconds,
    )

    fun pause()
}
