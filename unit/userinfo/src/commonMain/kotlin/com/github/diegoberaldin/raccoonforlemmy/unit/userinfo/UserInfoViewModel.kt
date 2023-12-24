package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserInfoViewModel(
    private val mvi: DefaultMviModel<UserInfoMviModel.Intent, UserInfoMviModel.UiState, UserInfoMviModel.Effect>,
    private val user: UserModel,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
) : UserInfoMviModel,
    MviModel<UserInfoMviModel.Intent, UserInfoMviModel.UiState, UserInfoMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(user = user)
        }
        mvi.scope?.launch(Dispatchers.IO) {
            settingsRepository.currentSettings.onEach {
                mvi.updateState { it.copy(autoLoadImages = it.autoLoadImages) }
            }.launchIn(this)


            if (uiState.value.moderatedCommunities.isEmpty()) {
                val updatedUser = userRepository.get(user.id)
                if (updatedUser != null) {
                    mvi.updateState {
                        it.copy(user = updatedUser)
                    }
                }
                val communities = userRepository.getModeratedCommunities(id = user.id)
                mvi.updateState { it.copy(moderatedCommunities = communities) }
            }
        }
    }
}