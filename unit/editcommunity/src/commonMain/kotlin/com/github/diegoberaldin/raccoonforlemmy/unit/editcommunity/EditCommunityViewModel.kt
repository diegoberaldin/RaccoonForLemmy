package com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class EditCommunityViewModel(
    private val communityId: Int,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
) : EditCommunityMviModel,
    DefaultMviModel<EditCommunityMviModel.Intent, EditCommunityMviModel.UiState, EditCommunityMviModel.Effect>(
        initialState = EditCommunityMviModel.UiState()
    ) {

    private var originalCommunity: CommunityModel? = null

    override fun reduce(intent: EditCommunityMviModel.Intent) {
        when (intent) {
            EditCommunityMviModel.Intent.Refresh -> screenModelScope.launch {
                refresh()
            }

            is EditCommunityMviModel.Intent.IconSelected -> loadImageIcon(intent.value)
            is EditCommunityMviModel.Intent.BannerSelected -> loadImageBanner(intent.value)

            is EditCommunityMviModel.Intent.ChangeDescription -> {
                updateState {
                    it.copy(
                        description = intent.value,
                        hasUnsavedChanges = true,
                    )
                }
            }

            is EditCommunityMviModel.Intent.ChangeTitle -> {
                updateState {
                    it.copy(
                        title = intent.value,
                        hasUnsavedChanges = true,
                    )
                }
            }

            is EditCommunityMviModel.Intent.ChangeNsfw -> {
                updateState {
                    it.copy(
                        nsfw = intent.value,
                        hasUnsavedChanges = true,
                    )
                }
            }

            is EditCommunityMviModel.Intent.ChangePostingRestrictedToMods -> {
                updateState {
                    it.copy(
                        postingRestrictedToMods = intent.value,
                        hasUnsavedChanges = true,
                    )
                }
            }

            EditCommunityMviModel.Intent.Submit -> submit()
        }
    }

    private suspend fun refresh() {
        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value.orEmpty()
        val community = communityRepository.get(
            auth = auth,
            id = communityId
        )
        originalCommunity = community
        if (community != null) {
            updateState {
                it.copy(
                    title = community.title,
                    icon = community.icon.orEmpty(),
                    banner = community.banner.orEmpty(),
                    description = community.description,
                    nsfw = community.nsfw,
                    postingRestrictedToMods = community.postingRestrictedToMods == true,
                    loading = false,
                )
            }
        }
    }

    private fun loadImageIcon(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                updateState {
                    it.copy(
                        icon = url,
                        hasUnsavedChanges = true,
                        loading = false,
                    )
                }
            }
        }
    }

    private fun loadImageBanner(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                updateState {
                    it.copy(
                        banner = url,
                        hasUnsavedChanges = true,
                        loading = false,
                    )
                }
            }
        }
    }

    private fun submit() {
        val community = originalCommunity?.copy() ?: return
        val currentState = uiState.value
        updateState { it.copy(loading = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                val newValue = community.copy(
                    title = currentState.title,
                    description = currentState.description,
                    icon = currentState.icon,
                    banner = currentState.banner,
                    nsfw = currentState.nsfw,
                    postingRestrictedToMods = currentState.postingRestrictedToMods,
                )
                communityRepository.update(
                    auth = auth,
                    community = newValue,
                )
                updateState {
                    it.copy(
                        loading = false,
                        hasUnsavedChanges = false,
                    )
                }
                emitEffect(
                    EditCommunityMviModel.Effect.Success
                )
            } catch (e: Exception) {
                updateState { it.copy(loading = false) }
                emitEffect(
                    EditCommunityMviModel.Effect.Failure
                )
            }
        }
    }
}
