package com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.LEMMY_COMMUNITY_INSTANCE
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutContants.LEMMY_COMMUNITY_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch


class AboutDialogViewModel(
    private val mvi: DefaultMviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
) : AboutDialogMviModel,
    MviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(
                version = AppInfo.versionCode,
            )
        }
    }

    override fun reduce(intent: AboutDialogMviModel.Intent) {
        when (intent) {
            AboutDialogMviModel.Intent.OpenOwnCommunity -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    val auth = identityRepository.authToken.value
                    val (community, instance) = (communityRepository.getSubscribed(auth)
                        .firstOrNull { it.name == LEMMY_COMMUNITY_NAME } to "").let {
                        if (it.first == null) {
                            communityRepository.get(
                                name = LEMMY_COMMUNITY_NAME, instance = LEMMY_COMMUNITY_INSTANCE
                            ) to LEMMY_COMMUNITY_INSTANCE
                        } else it
                    }

                    if (community != null) {
                        mvi.emitEffect(
                            AboutDialogMviModel.Effect.OpenCommunity(
                                community = community, instance = instance
                            )
                        )
                    }
                }
            }
        }
    }
}
