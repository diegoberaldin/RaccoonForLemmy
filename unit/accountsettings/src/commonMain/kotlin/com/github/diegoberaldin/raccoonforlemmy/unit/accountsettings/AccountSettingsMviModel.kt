package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

@Stable
interface AccountSettingsMviModel :
    MviModel<AccountSettingsMviModel.Intent, AccountSettingsMviModel.UiState, AccountSettingsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeDisplayName(val value: String) : Intent
        data class ChangeEmail(val value: String) : Intent
        data class ChangeMatrixUserId(val value: String) : Intent
        data class ChangeBio(val value: String) : Intent
        data class ChangeBot(val value: Boolean) : Intent
        data class ChangeSendNotificationsToEmail(val value: Boolean) : Intent
        data class ChangeShowBotAccounts(val value: Boolean) : Intent
        data class ChangeShowReadPosts(val value: Boolean) : Intent
        data class ChangeShowNsfw(val value: Boolean) : Intent
        data class ChangeShowScores(val value: Boolean) : Intent
        data class AvatarSelected(val value: ByteArray) : Intent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as AvatarSelected

                if (!value.contentEquals(other.value)) return false

                return true
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data class BannerSelected(val value: ByteArray) : Intent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as BannerSelected

                if (!value.contentEquals(other.value)) return false

                return true
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data object Submit : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val avatar: String = "",
        val banner: String = "",
        val bio: String = "",
        val bot: Boolean = false,
        val sendNotificationsToEmail: Boolean = false,
        val displayName: String = "",
        val matrixUserId: String = "",
        val email: String = "",
        val showBotAccounts: Boolean = false,
        val showReadPosts: Boolean = false,
        val showNsfw: Boolean = false,
        val showScores: Boolean = true,
        val defaultListingType: ListingType = ListingType.All,
        val availableSortTypes: List<SortType> = emptyList(),
        val defaultSortType: SortType = SortType.Active,
    )

    sealed interface Effect {
        data object Success : Effect
        data object Failure : Effect
    }
}
