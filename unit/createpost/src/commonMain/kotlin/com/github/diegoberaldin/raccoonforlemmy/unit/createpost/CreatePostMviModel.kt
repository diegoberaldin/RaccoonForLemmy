package com.github.diegoberaldin.raccoonforlemmy.unit.createpost

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

@Stable
interface CreatePostMviModel :
    MviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class SetCommunity(val value: CommunityModel) : Intent
        data class SetTitle(val value: String) : Intent
        data class SetUrl(val value: String) : Intent
        data class ChangeNsfw(val value: Boolean) : Intent
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

        data class InsertImageInBody(val value: ByteArray) : Intent {
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

        data class ChangeSection(val value: CreatePostSection) : Intent

        data class ChangeLanguage(val value: Int?) : Intent
        data class ChangeBodyValue(val value: TextFieldValue) : Intent
        data object Send : Intent
    }

    data class UiState(
        val editedPost: PostModel? = null,
        val crossPost: PostModel? = null,
        val communityInfo: String = "",
        val communityId: Int? = null,
        val communityError: ValidationError? = null,
        val title: String = "",
        val titleError: ValidationError? = null,
        val bodyValue: TextFieldValue = TextFieldValue(),
        val bodyError: ValidationError? = null,
        val url: String = "",
        val urlError: ValidationError? = null,
        val nsfw: Boolean = false,
        val loading: Boolean = false,
        val section: CreatePostSection = CreatePostSection.Edit,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val currentInstance: String = "",
        val currentUser: String = "",
        val currentLanguageId: Int? = null,
        val availableLanguages: List<LanguageModel> = emptyList(),
    )

    sealed interface Effect {
        data object Success : Effect
        data class Failure(val message: String?) : Effect
    }
}
