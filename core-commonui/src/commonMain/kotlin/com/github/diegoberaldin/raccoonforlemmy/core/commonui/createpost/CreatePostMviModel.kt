package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import dev.icerock.moko.resources.desc.StringDesc

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
        data class Send(val body: String) : Intent
    }

    data class UiState(
        val communityInfo: String = "",
        val communityId: Int? = null,
        val communityError: StringDesc? = null,
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
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val currentInstance: String = "",
        val currentUser: String = "",
    )

    sealed interface Effect {
        data class AddImageToBody(val url: String) : Effect
        data object Success : Effect
        data class Failure(val message: String?) : Effect
    }
}
