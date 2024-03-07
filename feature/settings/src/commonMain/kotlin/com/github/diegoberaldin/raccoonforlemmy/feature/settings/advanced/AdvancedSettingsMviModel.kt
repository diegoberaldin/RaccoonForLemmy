package com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface AdvancedSettingsMviModel :
    ScreenModel,
    MviModel<AdvancedSettingsMviModel.Intent, AdvancedSettingsMviModel.UiState, AdvancedSettingsMviModel.Effect> {

    sealed interface Intent {
        data class ChangeEnableDoubleTapAction(val value: Boolean) : Intent
        data class ChangeAutoLoadImages(val value: Boolean) : Intent
        data class ChangeAutoExpandComments(val value: Boolean) : Intent
        data class ChangeHideNavigationBarWhileScrolling(val value: Boolean) : Intent
        data class ChangeMarkAsReadWhileScrolling(val value: Boolean) : Intent
        data class ChangeNavBarTitlesVisible(val value: Boolean) : Intent
        data class ChangeSearchPostTitleOnly(val value: Boolean) : Intent
        data class ChangeEdgeToEdge(val value: Boolean) : Intent
        data class ChangeInfiniteScrollDisabled(val value: Boolean) : Intent
        data class ChangeImageSourcePath(val value: Boolean) : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val autoLoadImages: Boolean = false,
        val defaultInboxUnreadOnly: Boolean = true,
        val navBarTitlesVisible: Boolean = false,
        val enableDoubleTapAction: Boolean = true,
        val autoExpandComments: Boolean = false,
        val hideNavigationBarWhileScrolling: Boolean = true,
        val zombieModeInterval: Duration = 2.5.seconds,
        val zombieModeScrollAmount: Float = 100f,
        val markAsReadWhileScrolling: Boolean = true,
        val searchPostTitleOnly: Boolean = false,
        val edgeToEdge: Boolean = true,
        val infiniteScrollDisabled: Boolean = false,
        val opaqueSystemBars: Boolean = false,
        val imageSourcePath: Boolean = false,
    )

    sealed interface Effect
}