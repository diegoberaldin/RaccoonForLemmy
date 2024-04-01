package com.github.diegoberaldin.raccoonforlemmy.unit.drafts

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftType
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DraftRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DraftsViewModel(
    private val themeRepository: ThemeRepository,
    private val accountRepository: AccountRepository,
    private val draftRepository: DraftRepository,
    private val notificationCenter: NotificationCenter,
) : DraftsMviModel,
    DefaultMviModel<DraftsMviModel.Intent, DraftsMviModel.State, DraftsMviModel.Effect>(
        initialState = DraftsMviModel.State(),
    ) {

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.DraftDeleted::class)
                .onEach {
                    refresh()
                }.launchIn(this)

            if (uiState.value.postDrafts.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: DraftsMviModel.Intent) {
        when (intent) {
            is DraftsMviModel.Intent.ChangeSection -> updateState {
                it.copy(section = intent.section)
            }

            is DraftsMviModel.Intent.Delete -> deleteDraft(intent.model)
            DraftsMviModel.Intent.Refresh -> refresh()
        }
    }

    private fun refresh(initial: Boolean = false) {
        updateState {
            it.copy(
                refreshing = true,
                initial = initial,
            )
        }
        screenModelScope.launch {
            val currentState = uiState.value
            updateState { it.copy(loading = true) }
            val refreshing = currentState.refreshing
            val section = currentState.section
            val accountId = accountRepository.getActive()?.id ?: 0
            if (section == DraftsSection.Posts) {
                coroutineScope {
                    val itemList = async {
                        draftRepository.getAll(
                            type = DraftType.Post,
                            accountId = accountId,
                        )
                    }.await()
                    val commentDrafts = async {
                        if (initial && (currentState.commentDrafts.isEmpty() || refreshing)) {
                            // this is needed because otherwise on first selector change
                            // the lazy column scrolls back to top (it must have an empty data set)
                            draftRepository.getAll(
                                type = DraftType.Comment,
                                accountId = accountId,
                            )
                        } else {
                            currentState.commentDrafts
                        }
                    }.await()
                    updateState {
                        val postDrafts = if (refreshing) {
                            itemList
                        } else {
                            it.postDrafts + itemList
                        }
                        it.copy(
                            postDrafts = postDrafts,
                            commentDrafts = commentDrafts,
                            loading = false,
                            refreshing = false,
                            initial = false,
                        )
                    }
                }
            } else {
                val itemList = draftRepository.getAll(
                    type = DraftType.Comment,
                    accountId = accountId,
                )
                updateState {
                    val commentDrafts = if (refreshing) {
                        itemList
                    } else {
                        it.commentDrafts + itemList
                    }
                    it.copy(
                        commentDrafts = commentDrafts,
                        loading = false,
                        refreshing = false,
                        initial = false,
                    )
                }
            }
        }
    }

    private fun deleteDraft(model: DraftModel) {
        screenModelScope.launch {
            model.id?.also { id ->
                draftRepository.delete(id)
            }
            updateState {
                if (model.type == DraftType.Post) {
                    it.copy(
                        postDrafts = it.postDrafts.filter { e -> e.id != model.id }
                    )
                } else {
                    it.copy(
                        commentDrafts = it.commentDrafts.filter { e -> e.id != model.id }
                    )
                }
            }
        }
    }
}
