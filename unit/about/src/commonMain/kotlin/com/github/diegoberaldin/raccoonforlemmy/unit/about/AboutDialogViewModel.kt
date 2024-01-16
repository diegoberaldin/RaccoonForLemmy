package com.github.diegoberaldin.raccoonforlemmy.unit.about

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.LEMMY_COMMUNITY_INSTANCE
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutConstants.LEMMY_COMMUNITY_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


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
                    val (community, instance) = searchCommunity().let { community ->
                        if (community != null) {
                            community to ""
                        } else {
                            communityRepository.get(
                                name = LEMMY_COMMUNITY_NAME,
                                instance = LEMMY_COMMUNITY_INSTANCE
                            ) to LEMMY_COMMUNITY_INSTANCE
                        }
                    }

                    if (community != null) {
                        mvi.emitEffect(
                            AboutDialogMviModel.Effect.OpenCommunity(
                                community = community,
                                instance = instance
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun searchCommunity(): CommunityModel? {
        val auth = identityRepository.authToken.value
        suspend fun searchRec(page: Int = 0): CommunityModel? {
            return communityRepository.getAll(
                auth = auth,
                query = LEMMY_COMMUNITY_NAME,
                resultType = SearchResultType.Communities,
                page = page,
                limit = 50,
            )
                ?.filterIsInstance<SearchResult.Community>()
                ?.firstOrNull {
                    it.model.name == LEMMY_COMMUNITY_NAME
                }?.model ?: searchRec(page + 1)
        }
        return withTimeoutOrNull(5000) {
            // start recursive search
            searchRec()
        }
    }
}
