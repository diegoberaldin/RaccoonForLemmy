package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostSection
import dev.icerock.moko.resources.desc.StringDesc

@Stable
interface CreateCommentMviModel :
    MviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeSection(val value: CreatePostSection) : Intent
        data class ImageSelected(val value: ByteArray) : Intent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as ImageSelected

                return value.contentEquals(other.value)
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data class Send(val text: String) : Intent
    }

    data class UiState(
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val textError: StringDesc? = null,
        val loading: Boolean = false,
        val section: CreatePostSection = CreatePostSection.Edit,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data class AddImageToText(val url: String) : Effect
        data class Success(val new: Boolean) : Effect
        data class Failure(val message: String?) : Effect
    }
}
