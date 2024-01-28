package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountSettingsModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class AccountSettingsViewModel(
    private val mvi: DefaultMviModel<AccountSettingsMviModel.Intent, AccountSettingsMviModel.UiState, AccountSettingsMviModel.Effect>,
    private val siteRepository: SiteRepository,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val notificationCenter: NotificationCenter,
) : AccountSettingsMviModel,
    MviModel<AccountSettingsMviModel.Intent, AccountSettingsMviModel.UiState, AccountSettingsMviModel.Effect> by mvi {

    private var accountSettings: AccountSettingsModel? = null

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    mvi.updateState { it.copy(defaultSortType = evt.value) }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeFeedType::class)
                .onEach { evt ->
                    mvi.updateState { it.copy(defaultListingType = evt.value) }
                }.launchIn(this)

            if (accountSettings == null) {
                refreshSettings()
                val availableSortTypes = getSortTypesUseCase.getTypesForPosts()
                mvi.updateState { it.copy(availableSortTypes = availableSortTypes) }
            }
        }
    }

    override fun reduce(intent: AccountSettingsMviModel.Intent) {
        when (intent) {
            is AccountSettingsMviModel.Intent.ChangeDisplayName -> {
                mvi.updateState { it.copy(displayName = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeEmail -> {
                mvi.updateState { it.copy(email = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeMatrixUserId -> {
                mvi.updateState { it.copy(matrixUserId = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeBio -> {
                mvi.updateState { it.copy(bio = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeBot -> {
                mvi.updateState { it.copy(bot = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeSendNotificationsToEmail -> {
                mvi.updateState { it.copy(sendNotificationsToEmail = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeShowBotAccounts -> {
                mvi.updateState { it.copy(showBotAccounts = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeShowNsfw -> {
                mvi.updateState { it.copy(showNsfw = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeShowScores -> {
                mvi.updateState { it.copy(showScores = intent.value) }
            }

            is AccountSettingsMviModel.Intent.ChangeShowReadPosts -> {
                mvi.updateState { it.copy(showReadPosts = intent.value) }
            }

            is AccountSettingsMviModel.Intent.AvatarSelected -> {
                loadImageAvatar(intent.value)
            }

            is AccountSettingsMviModel.Intent.BannerSelected -> {
                loadImageBanner(intent.value)
            }

            AccountSettingsMviModel.Intent.Submit -> submit()
        }
    }

    private suspend fun refreshSettings() {
        mvi.updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value.orEmpty()
        accountSettings = siteRepository.getAccountSettings(auth)
        mvi.updateState {
            it.copy(
                loading = false,
                avatar = accountSettings?.avatar.orEmpty(),
                banner = accountSettings?.banner.orEmpty(),
                bio = accountSettings?.bio.orEmpty(),
                bot = accountSettings?.bot ?: false,
                sendNotificationsToEmail = accountSettings?.sendNotificationsToEmail ?: false,
                displayName = accountSettings?.displayName.orEmpty(),
                matrixUserId = accountSettings?.matrixUserId.orEmpty(),
                email = accountSettings?.email.orEmpty(),
                showBotAccounts = accountSettings?.showBotAccounts ?: false,
                showReadPosts = accountSettings?.showReadPosts ?: false,
                showNsfw = accountSettings?.showNsfw ?: false,
                showScores = accountSettings?.showScores ?: true,
                defaultListingType = accountSettings?.defaultListingType ?: ListingType.All,
                defaultSortType = accountSettings?.defaultSortType ?: SortType.Active,
            )
        }
    }

    private fun loadImageAvatar(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                mvi.updateState {
                    it.copy(
                        avatar = url,
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
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                mvi.updateState {
                    it.copy(
                        banner = url,
                        loading = false,
                    )
                }
            }
        }
    }

    private fun submit() {
        val currentState = uiState.value
        val settingsToSave = accountSettings?.copy(
            avatar = currentState.avatar,
            banner = currentState.banner,
            bio = currentState.bio,
            bot = currentState.bot,
            defaultListingType = currentState.defaultListingType,
            defaultSortType = currentState.defaultSortType,
            displayName = currentState.displayName,
            email = currentState.email,
            matrixUserId = currentState.matrixUserId,
            sendNotificationsToEmail = currentState.sendNotificationsToEmail,
            showBotAccounts = currentState.showBotAccounts,
            showNsfw = currentState.showNsfw,
            showScores = currentState.showScores,
            showReadPosts = currentState.showReadPosts,
        ) ?: return
        mvi.updateState { it.copy(loading = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                siteRepository.updateAccountSettings(
                    auth = auth,
                    value = settingsToSave,
                )
                refreshSettings()
                mvi.emitEffect(
                    AccountSettingsMviModel.Effect.Success
                )
            } catch (e: Exception) {
                mvi.updateState { it.copy(loading = false) }
                mvi.emitEffect(
                    AccountSettingsMviModel.Effect.Failure
                )
            }
        }
    }
}
