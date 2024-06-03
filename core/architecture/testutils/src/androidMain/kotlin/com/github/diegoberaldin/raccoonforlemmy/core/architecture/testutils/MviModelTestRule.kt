package com.github.diegoberaldin.raccoonforlemmy.core.architecture.testutils

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import kotlinx.coroutines.flow.Flow
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MviModelTestRule<Intent, State, Effect>(
    private val initBlock: () -> MviModel<Intent, State, Effect>
) : TestWatcher() {

    private lateinit var model: MviModel<Intent, State, Effect>

    override fun starting(description: Description) {
        model = initBlock()
    }

    fun send(intent: Intent) {
        model.reduce(intent)
    }

    fun onState(block: (State) -> Unit) {
        block(model.uiState.value)
    }

    suspend fun onStates(block: suspend (Flow<State>) -> Unit) {
        block(model.uiState)
    }

    suspend fun onEffects(block: suspend (Flow<Effect>) -> Unit) {
        block(model.effects)
    }
}
