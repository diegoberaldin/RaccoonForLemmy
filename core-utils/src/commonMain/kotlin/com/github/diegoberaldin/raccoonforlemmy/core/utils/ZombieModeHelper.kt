package com.github.diegoberaldin.raccoonforlemmy.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

internal class DefaultZombieModeHelper : ZombieModeHelper {
    private val scope = CoroutineScope(SupervisorJob())
    override val index = MutableStateFlow(-1)
    private var delayInterval: Duration = 2.5.seconds
    private var job: Job? = null

    override fun start(initialValue: Int, interval: Duration) {
        index.value = initialValue
        delayInterval = interval
        job = scope.launch {
            while (isActive) {
                delay(delayInterval)
                index.update { (it + 1).coerceAtLeast(0) }
            }
        }
    }

    override fun pause() {
        job?.cancel()
        index.value = -1
    }
}