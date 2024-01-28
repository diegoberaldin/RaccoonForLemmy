package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
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

        data class ChangeLanguage(val value: Int?) : Intent

        data class Send(val text: String) : Intent
    }

    data class UiState(
        val originalPost: PostModel? = null,
        val originalComment: CommentModel? = null,
        val editedComment: CommentModel? = null,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val textError: StringDesc? = null,
        val loading: Boolean = false,
        val section: CreatePostSection = CreatePostSection.Edit,
        val autoLoadImages: Boolean = true,
        val showScores: Boolean = true,
        val currentInstance: String = "",
        val currentUser: String = "",
        val currentLanguageId: Int? = null,
        val availableLanguages: List<LanguageModel> = emptyList(),
    )

    sealed interface Effect {
        data class AddImageToText(val url: String) : Effect
        data class Success(val new: Boolean) : Effect
        data class Failure(val message: String?) : Effect
    }
}
