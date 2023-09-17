package com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class InstanceInfoViewModel(
    private val mvi: DefaultMviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>,
    private val url: String,
    private val siteRepository: SiteRepository,
    private val communityRepository: CommunityRepository,
    private val identityRepository: IdentityRepository,
) : ScreenModel,
    MviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect> by mvi {

    private var currentPage = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.IO) {
            val metadata = siteRepository.getMetadata(url)
            if (metadata != null) {
                mvi.updateState {
                    it.copy(
                        title = metadata.title,
                        description = metadata.description,
                    )
                }
            }
        }

        if (mvi.uiState.value.communities.isEmpty()) {
            refresh()
        }
    }

    override fun reduce(intent: InstanceInfoMviModel.Intent) {
        when (intent) {
            InstanceInfoMviModel.Intent.LoadNextPage -> loadNextPage()
            InstanceInfoMviModel.Intent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val instance = url.replace("https://", "")
            val itemList = communityRepository.getAllInInstance(
                auth = auth,
                instance = instance,
                page = currentPage,
            )
            currentPage++
            val canFetchMore = itemList.size >= CommentRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newItems = if (refreshing) {
                    itemList.filter { e -> e.instanceUrl == url }
                } else {
                    it.communities + itemList.filter { e -> e.instanceUrl == url }
                }
                it.copy(
                    communities = newItems,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }
}
