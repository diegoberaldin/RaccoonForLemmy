package com.github.diegoberaldin.raccoonforlemmy.core.architecture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Basic implementation of the MVI model.
 * The [updateState] and [emitEffect] methods are shortcuts to easily update the UI state and emit a side effect.
 *
 * @param Intent class of intents
 * @param State class of UI state
 * @param Effect class of effects
 * @constructor Create [DefaultMviModel]
 *
 * @param initialState initial UI state
 */
abstract class DefaultMviModel<Intent, State, Effect>(
    initialState: State,
) : MviModel<Intent, State, Effect> {
    override val uiState = MutableStateFlow(initialState)
    override val effects = MutableSharedFlow<Effect>()
    private val mutex = Mutex()

    /**
     * Emit an effect (event).
     *
     * @param value Value
     */
    protected suspend fun emitEffect(value: Effect) {
        effects.emit(value)
    }

    /**
     * Update the UI state.
     *
     * @param block Block
     */
    protected suspend fun updateState(block: (State) -> State) {
        mutex.withLock {
            uiState.update { block(uiState.value) }
        }
    }

    override fun reduce(intent: Intent) {
        // Noop
    }
}
