package com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConfigureContentViewViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
) : ConfigureContentViewMviModel,
    DefaultMviModel<ConfigureContentViewMviModel.Intent, ConfigureContentViewMviModel.State, ConfigureContentViewMviModel.Effect>(
        initialState = ConfigureContentViewMviModel.State()
    ) {

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { value ->
                updateState { it.copy(postLayout = value) }
            }.launchIn(this)
            themeRepository.contentFontScale.onEach { value ->
                updateState { it.copy(contentFontScale = value) }
            }.launchIn(this)
            themeRepository.contentFontFamily.onEach { value ->
                updateState { it.copy(contentFontFamily = value) }
            }.launchIn(this)
            themeRepository.commentBarThickness.onEach { value ->
                updateState { it.copy(commentBarThickness = value) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ChangePostLayout::class)
                .onEach { evt ->
                    changePostLayout(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeVoteFormat::class)
                .onEach { evt ->
                    changeVoteFormat(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangePostBodyMaxLines::class)
                .onEach { evt ->
                    changePostBodyMaxLines(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeContentFontSize::class)
                .onEach { evt ->
                    changeContentFontScale(evt.value, evt.contentClass)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeContentFontFamily::class)
                .onEach { evt ->
                    changeContentFontFamily(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentBarThickness::class)
                .onEach { evt ->
                    changeCommentBarThickness(evt.value)
                }.launchIn(this)

            val settings = settingsRepository.currentSettings.value
            updateState {
                it.copy(
                    voteFormat = if (!settings.showScores) VoteFormat.Hidden else settings.voteFormat,
                    fullHeightImages = settings.fullHeightImages,
                    postBodyMaxLines = settings.postBodyMaxLines,
                    preferUserNicknames = settings.preferUserNicknames,
                )
            }
        }
    }

    override fun reduce(intent: ConfigureContentViewMviModel.Intent) {
        when (intent) {
            is ConfigureContentViewMviModel.Intent.ChangeFullHeightImages -> changeFullHeightImages(
                intent.value
            )

            is ConfigureContentViewMviModel.Intent.ChangePreferUserNicknames -> changePreferUserNicknames(
                intent.value
            )
        }
    }

    private fun changePostLayout(value: PostLayout) {
        themeRepository.changePostLayout(value)
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                postLayout = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeVoteFormat(value: VoteFormat) {
        updateState { it.copy(voteFormat = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.let {
                if (value == VoteFormat.Hidden) {
                    it.copy(showScores = false)
                } else {
                    it.copy(
                        voteFormat = value,
                        showScores = true,
                    )
                }
            }
            saveSettings(settings)
        }
    }

    private fun changeFullHeightImages(value: Boolean) {
        updateState { it.copy(fullHeightImages = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                fullHeightImages = value
            )
            saveSettings(settings)
        }
    }

    private fun changePreferUserNicknames(value: Boolean) {
        updateState { it.copy(preferUserNicknames = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                preferUserNicknames = value
            )
            saveSettings(settings)
        }
    }

    private fun changePostBodyMaxLines(value: Int?) {
        updateState { it.copy(postBodyMaxLines = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                postBodyMaxLines = value
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontScale(value: Float, contentClass: ContentFontClass) {
        val contentFontScale = themeRepository.contentFontScale.value.let {
            when (contentClass) {
                ContentFontClass.Title -> it.copy(title = value)
                ContentFontClass.Body -> it.copy(body = value)
                ContentFontClass.Comment -> it.copy(comment = value)
                ContentFontClass.AncillaryText -> it.copy(ancillary = value)
            }
        }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontScale = contentFontScale
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontFamily(value: UiFontFamily) {
        themeRepository.changeContentFontFamily(value)
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontFamily = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeCommentBarThickness(value: Int) {
        themeRepository.changeCommentBarThickness(value)
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                commentBarThickness = value
            )
            saveSettings(settings)
        }
    }

    private suspend fun saveSettings(settings: SettingsModel) {
        val accountId = accountRepository.getActive()?.id
        settingsRepository.updateSettings(settings, accountId)
        settingsRepository.changeCurrentSettings(settings)
    }
}