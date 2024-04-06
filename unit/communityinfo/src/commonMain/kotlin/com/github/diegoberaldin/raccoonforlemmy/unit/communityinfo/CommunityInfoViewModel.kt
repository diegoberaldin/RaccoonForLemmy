package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CommunityInfoViewModel(
    private val communityId: Long,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
    private val itemCache: LemmyItemCache,
) : CommunityInfoMviModel,
    DefaultMviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect>(
        initialState = CommunityInfoMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch {
            if (uiState.value.community.id == 0L) {
                val community = itemCache.getCommunity(communityId) ?: CommunityModel()
                updateState { it.copy(community = community) }
            }
            settingsRepository.currentSettings.onEach {
                updateState { it.copy(autoLoadImages = it.autoLoadImages) }
            }.launchIn(this)

            if (uiState.value.moderators.isEmpty()) {
                val community = communityRepository.get(id = communityId)
                if (community != null) {
                    updateState { it.copy(community = community) }
                }
                val moderators = communityRepository.getModerators(
                    id = communityId,
                )
                updateState { it.copy(moderators = moderators) }
            }
        }
    }
}
