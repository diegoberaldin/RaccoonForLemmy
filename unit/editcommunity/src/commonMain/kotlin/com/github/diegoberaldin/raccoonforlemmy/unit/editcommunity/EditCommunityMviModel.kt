package com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface EditCommunityMviModel : ScreenModel,
    MviModel<EditCommunityMviModel.Intent, EditCommunityMviModel.UiState, EditCommunityMviModel.Effect> {

    sealed interface Intent {
        data object Refresh : Intent
        data class ChangeTitle(val value: String) : Intent
        data class ChangeDescription(val value: String) : Intent

        data class IconSelected(val value: ByteArray) : Intent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as IconSelected

                return value.contentEquals(other.value)
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

                return value.contentEquals(other.value)
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data class ChangeNsfw(val value: Boolean) : Intent
        data class ChangePostingRestrictedToMods(val value: Boolean) : Intent
        data object Submit : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val title: String = "",
        val description: String = "",
        val icon: String = "",
        val banner: String = "",
        val hasUnsavedChanges: Boolean = false,
        val nsfw: Boolean = false,
        val postingRestrictedToMods: Boolean = false,
    )

    sealed interface Effect {
        data object Success : Effect
        data object Failure : Effect
    }
}
