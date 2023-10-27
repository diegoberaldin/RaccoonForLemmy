package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

interface CreatePostMviModel :
    MviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class SetTitle(val value: String) : Intent
        data class SetUrl(val value: String) : Intent
        data class ChangeNsfw(val value: Boolean) : Intent
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

        data class InsertImageInBody(val value: ByteArray) : Intent {
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

        data class ChangeSection(val value: CreatePostSection) : Intent
        data class Send(val body: String) : Intent
    }

    data class UiState(
        val title: String = "",
        val titleError: StringDesc? = null,
        val bodyError: StringDesc? = null,
        val url: String = "",
        val urlError: StringDesc? = null,
        val nsfw: Boolean = false,
        val loading: Boolean = false,
        val section: CreatePostSection = CreatePostSection.Edit,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val separateUpAndDownVotes: Boolean = false,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data class AddImageToBody(val url: String) : Effect
        data object Success : Effect
        data class Failure(val message: String?) : Effect
    }
}
