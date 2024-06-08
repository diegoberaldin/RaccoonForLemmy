package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.repository.AcknowledgementsRepository
import kotlinx.coroutines.launch

internal class AcknowledgementsViewModel(
    private val acknowledgementsRepository: AcknowledgementsRepository,
) : AcknowledgementsMviModel,
    DefaultMviModel<AcknowledgementsMviModel.Intent, AcknowledgementsMviModel.State, AcknowledgementsMviModel.Effect>(
        initialState = AcknowledgementsMviModel.State(),
    ) {
    init {
        screenModelScope.launch {
            refresh(initial = true)
        }
    }

    override fun reduce(intent: AcknowledgementsMviModel.Intent) {
        when (intent) {
            AcknowledgementsMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        updateState {
            it.copy(
                initial = initial,
                refreshing = !initial,
            )
        }
        val items = acknowledgementsRepository.getAcknowledgements()
        updateState {
            it.copy(
                items = items.orEmpty(),
                initial = false,
                refreshing = false,
            )
        }
    }
}
