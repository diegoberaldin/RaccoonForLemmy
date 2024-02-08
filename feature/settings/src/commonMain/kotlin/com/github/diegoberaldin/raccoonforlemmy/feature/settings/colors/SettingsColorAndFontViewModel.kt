package com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsColorAndFontViewModel(
    private val themeRepository: ThemeRepository,
    private val colorSchemeProvider: ColorSchemeProvider,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
) : SettingsColorAndFontMviModel,
    DefaultMviModel<SettingsColorAndFontMviModel.Intent, SettingsColorAndFontMviModel.UiState, SettingsColorAndFontMviModel.Effect>(
        initialState = SettingsColorAndFontMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            themeRepository.uiTheme.onEach { value ->
                updateState { it.copy(uiTheme = value) }
            }.launchIn(this)
            themeRepository.uiFontFamily.onEach { value ->
                updateState { it.copy(uiFontFamily = value) }
            }.launchIn(this)
            themeRepository.contentFontScale.onEach { value ->
                updateState { it.copy(contentFontScale = value.toFontScale()) }
            }.launchIn(this)
            themeRepository.contentFontFamily.onEach { value ->
                updateState { it.copy(contentFontFamily = value) }
            }.launchIn(this)
            themeRepository.uiFontScale.onEach { value ->
                updateState { it.copy(uiFontScale = value.toFontScale()) }
            }.launchIn(this)
            themeRepository.dynamicColors.onEach { value ->
                updateState { it.copy(dynamicColors = value) }
            }.launchIn(this)
            themeRepository.customSeedColor.onEach { value ->
                updateState { it.copy(customSeedColor = value) }
            }.launchIn(this)
            themeRepository.upVoteColor.onEach { value ->
                updateState { it.copy(upVoteColor = value) }
            }.launchIn(this)
            themeRepository.downVoteColor.onEach { value ->
                updateState { it.copy(downVoteColor = value) }
            }.launchIn(this)
            themeRepository.replyColor.onEach { value ->
                updateState { it.copy(replyColor = value) }
            }.launchIn(this)
            themeRepository.saveColor.onEach { value ->
                updateState { it.copy(saveColor = value) }
            }.launchIn(this)
            themeRepository.commentBarTheme.onEach { value ->
                updateState { it.copy(commentBarTheme = value) }
            }.launchIn(this)
            themeRepository.commentBarThickness.onEach { value ->
                updateState { it.copy(commentBarThickness = value) }
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ChangeFontFamily::class)
                .onEach { evt ->
                    changeFontFamily(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeContentFontSize::class)
                .onEach { evt ->
                    changeContentFontScale(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeContentFontFamily::class)
                .onEach { evt ->
                    changeContentFontFamily(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeUiFontSize::class)
                .onEach { evt ->
                    changeUiFontScale(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeColor::class).onEach { evt ->
                changeCustomSeedColor(evt.color)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentBarTheme::class)
                .onEach { evt ->
                    changeCommentBarTheme(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentBarThickness::class)
                .onEach { evt ->
                    changeCommentBarThickness(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeActionColor::class)
                .onEach { evt ->
                    when (evt.actionType) {
                        3 -> changeSaveColor(evt.color)
                        2 -> changeReplyColor(evt.color)
                        1 -> changeDownVoteColor(evt.color)
                        else -> changeUpVoteColor(evt.color)
                    }
                }.launchIn(this)
            updateState {
                it.copy(
                    supportsDynamicColors = colorSchemeProvider.supportsDynamicColors,
                )
            }
        }
    }

    override fun reduce(intent: SettingsColorAndFontMviModel.Intent) {
        when (intent) {
            is SettingsColorAndFontMviModel.Intent.ChangeDynamicColors -> {
                changeDynamicColors(intent.value)
            }
        }
    }

    private fun changeFontFamily(value: UiFontFamily) {
        themeRepository.changeUiFontFamily(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                uiFontFamily = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeUiFontScale(value: Float) {
        themeRepository.changeUiFontScale(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                uiFontScale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontScale(value: Float) {
        themeRepository.changeContentFontScale(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontScale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontFamily(value: UiFontFamily) {
        themeRepository.changeContentFontFamily(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontFamily = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeDynamicColors(value: Boolean) {
        themeRepository.changeDynamicColors(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                dynamicColors = value
            )
            saveSettings(settings)
        }
    }

    private fun changeCustomSeedColor(value: Color?) {
        themeRepository.changeCustomSeedColor(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                customSeedColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeUpVoteColor(value: Color?) {
        themeRepository.changeUpVoteColor(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                upVoteColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeDownVoteColor(value: Color?) {
        themeRepository.changeDownVoteColor(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                downVoteColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeReplyColor(value: Color?) {
        themeRepository.changeReplyColor(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                replyColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeSaveColor(value: Color?) {
        themeRepository.changeSaveColor(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                saveColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeCommentBarTheme(value: CommentBarTheme) {
        themeRepository.changeCommentBarTheme(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                commentBarTheme = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeCommentBarThickness(value: Int) {
        themeRepository.changeCommentBarThickness(value)
        scope?.launch(Dispatchers.IO) {
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