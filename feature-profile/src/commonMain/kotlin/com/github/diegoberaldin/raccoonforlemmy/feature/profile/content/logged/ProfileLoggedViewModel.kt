package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ProfileLoggedViewModel(
    private val mvi: DefaultMviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
) : ScreenModel,
    MviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect> by mvi {
    override fun onStarted() {
        mvi.onStarted()
        val auth = identityRepository.authToken.value.orEmpty()
        mvi.scope?.launch(Dispatchers.IO) {
            val user = siteRepository.getCurrentUser(auth)
            mvi.updateState { it.copy(user = user) }
        }
    }

    override fun reduce(intent: ProfileLoggedMviModel.Intent) {
        when (intent) {
            is ProfileLoggedMviModel.Intent.SelectTab -> mvi.updateState {
                it.copy(currentTab = intent.value)
            }
        }
    }
}
