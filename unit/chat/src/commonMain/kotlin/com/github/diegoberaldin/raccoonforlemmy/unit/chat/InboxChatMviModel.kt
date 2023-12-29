package com.github.diegoberaldin.raccoonforlemmy.unit.chat

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

@Stable
interface InboxChatMviModel :
    MviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object LoadNextPage : Intent

        data class ImageSelected(val value: ByteArray) : Intent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as ImageSelected

                if (!value.contentEquals(other.value)) return false

                return true
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data class EditMessage(val value: Int) : Intent
        data class DeleteMessage(val value: Int) : Intent
        data class SubmitNewMessage(val value: String) : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val currentUserId: Int? = null,
        val otherUserName: String = "",
        val otherUserAvatar: String? = null,
        val messages: List<PrivateMessageModel> = emptyList(),
        val autoLoadImages: Boolean = true,
        val editedMessageId: Int? = null,
    )

    sealed interface Effect {
        data class AddImageToText(val url: String) : Effect
    }
}