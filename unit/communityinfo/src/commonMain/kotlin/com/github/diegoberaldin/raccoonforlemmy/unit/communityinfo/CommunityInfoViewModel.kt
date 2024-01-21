package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CommunityInfoViewModel(
    private val mvi: DefaultMviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect>,
    private val communityId: Int,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
    private val itemCache: LemmyItemCache,
) : CommunityInfoMviModel,
    MviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch {
            if (uiState.value.community.id == 0) {
                val community = itemCache.getCommunity(communityId) ?: CommunityModel()
                mvi.updateState { it.copy(community = community) }
            }
            settingsRepository.currentSettings.onEach {
                mvi.updateState { it.copy(autoLoadImages = it.autoLoadImages) }
            }.launchIn(this)

            if (uiState.value.moderators.isEmpty()) {
                val community = communityRepository.get(id = communityId)
                if (community != null) {
                    mvi.updateState { it.copy(community = community) }
                }
                val moderators = communityRepository.getModerators(
                    id = communityId,
                )
                mvi.updateState { it.copy(moderators = moderators) }
            }
        }
    }
}
