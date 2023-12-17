package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CommunityInfoViewModel(
    private val mvi: DefaultMviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect>,
    private val community: CommunityModel,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
) : CommunityInfoMviModel,
    MviModel<CommunityInfoMviModel.Intent, CommunityInfoMviModel.UiState, CommunityInfoMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState { it.copy(community = community) }

        mvi.scope?.launch(Dispatchers.IO) {
            settingsRepository.currentSettings.onEach {
                mvi.updateState { it.copy(autoLoadImages = it.autoLoadImages) }
            }.launchIn(this)

            if (uiState.value.moderators.isEmpty()) {
                val moderators = communityRepository.getModerators(
                    id = community.id
                )
                mvi.updateState { it.copy(moderators = moderators) }
            }
        }
    }
}
