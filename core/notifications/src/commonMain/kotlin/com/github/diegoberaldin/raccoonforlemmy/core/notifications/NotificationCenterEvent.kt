package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeDirection
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeTarget
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import kotlin.time.Duration

sealed interface NotificationCenterEvent {
    data class ChangeSortType(val value: SortType, val defaultForCommunity: Boolean, val screenKey: String?) :
        NotificationCenterEvent

    data class ChangeCommentSortType(val value: SortType, val screenKey: String?) :
        NotificationCenterEvent

    data class ChangeFeedType(val value: ListingType, val screenKey: String?) :
        NotificationCenterEvent

    data class ChangeInboxType(val unreadOnly: Boolean) : NotificationCenterEvent
    data class ChangeTheme(val value: UiTheme?) : NotificationCenterEvent
    data class ChangeContentFontSize(val value: Float, val contentClass: ContentFontClass) :
        NotificationCenterEvent

    data class ChangeUiFontSize(val value: Float) : NotificationCenterEvent
    data class ChangeFontFamily(val value: UiFontFamily) : NotificationCenterEvent
    data class ChangeContentFontFamily(val value: UiFontFamily) : NotificationCenterEvent
    data class ChangeZombieInterval(val value: Duration) : NotificationCenterEvent
    data class ChangeLanguage(val value: String) : NotificationCenterEvent
    data class ChangePostLayout(val value: PostLayout) : NotificationCenterEvent
    data class ChangeVoteFormat(val value: VoteFormat) : NotificationCenterEvent
    data object Logout : NotificationCenterEvent
    data object PostCreated : NotificationCenterEvent
    data object CommentCreated : NotificationCenterEvent
    data class PostUpdated(val model: PostModel) : NotificationCenterEvent
    data class CommentUpdated(val model: CommentModel) : NotificationCenterEvent
    data class PostDeleted(val model: PostModel) : NotificationCenterEvent
    data class ChangeColor(val color: Color?) : NotificationCenterEvent
    data class ChangeActionColor(val color: Color?, val actionType: Int) : NotificationCenterEvent
    data class ChangeZombieScrollAmount(val value: Float) : NotificationCenterEvent
    data class MultiCommunityCreated(val model: MultiCommunityModel) : NotificationCenterEvent
    data object CloseDialog : NotificationCenterEvent
    data class SelectCommunity(val model: CommunityModel) : NotificationCenterEvent
    data class PostRemoved(val model: PostModel) : NotificationCenterEvent
    data class CommentRemoved(val model: CommentModel) : NotificationCenterEvent
    data class ChangeReportListType(val unresolvedOnly: Boolean) : NotificationCenterEvent
    data class UserBannedPost(val postId: Long, val user: UserModel) : NotificationCenterEvent
    data class UserBannedComment(val commentId: Long, val user: UserModel) : NotificationCenterEvent
    data class ChangeCommentBarTheme(val value: CommentBarTheme) : NotificationCenterEvent
    data class ChangeCommentBarThickness(val value: Int) : NotificationCenterEvent
    data class BlockActionSelected(
        val userId: Long? = null,
        val communityId: Long? = null,
        val instanceId: Long? = null,
    ) : NotificationCenterEvent

    data class Share(val url: String) : NotificationCenterEvent
    data class ChangePostBodyMaxLines(val value: Int?) : NotificationCenterEvent
    data class InstanceSelected(val value: String) : NotificationCenterEvent
    data class ActionsOnSwipeSelected(
        val value: ActionOnSwipe,
        val direction: ActionOnSwipeDirection,
        val target: ActionOnSwipeTarget,
    ) : NotificationCenterEvent

    data class ChangeSystemBarTheme(val value: UiBarTheme) : NotificationCenterEvent
    data object DraftDeleted : NotificationCenterEvent
    data class ModeratorZoneActionSelected(val value: Int) : NotificationCenterEvent

    data object ResetHome : NotificationCenterEvent
    data object ResetExplore : NotificationCenterEvent
    data object ResetInbox : NotificationCenterEvent

    data class CopyText(val value: String) : NotificationCenterEvent
    data class ChangedLikedType(val value: Boolean) : NotificationCenterEvent

    data class ChangeSearchResultType(val value: SearchResultType, val screenKey: String?) :
        NotificationCenterEvent

    data class CommunitySubscriptionChanged(val value: CommunityModel) : NotificationCenterEvent

    sealed interface ShareImageModeSelected : NotificationCenterEvent {
        data class ModeUrl(val url: String) : ShareImageModeSelected
        data class ModeFile(val url: String, val source: String) : ShareImageModeSelected
    }
}
