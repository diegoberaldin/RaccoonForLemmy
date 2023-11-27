package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import kotlin.time.Duration

sealed interface NotificationCenterEvent {
    data class ChangeSortType(val value: SortType, val key: String? = null) :
        NotificationCenterEvent

    data class ChangeCommentSortType(val value: SortType, val key: String? = null) :
        NotificationCenterEvent

    data class ChangeFeedType(val value: ListingType, val key: String? = null) :
        NotificationCenterEvent

    data class ChangeInboxType(val unreadOnly: Boolean) : NotificationCenterEvent
    data class ChangeTheme(val value: UiTheme) : NotificationCenterEvent
    data class ChangeContentFontSize(val value: Float) : NotificationCenterEvent
    data class ChangeUiFontSize(val value: Float) : NotificationCenterEvent
    data class ChangeFontFamily(val value: UiFontFamily) : NotificationCenterEvent
    data class ChangeZombieInterval(val value: Duration) : NotificationCenterEvent
    data class ChangeLanguage(val value: String) : NotificationCenterEvent
    data class ChangePostLayout(val value: PostLayout) : NotificationCenterEvent
    data object Logout : NotificationCenterEvent
    data object PostCreated : NotificationCenterEvent
    data object CommentCreated : NotificationCenterEvent
    data class PostUpdated(val model: PostModel) : NotificationCenterEvent
    data class CommentUpdated(val model: CommentModel) : NotificationCenterEvent
    data class PostDeleted(val model: PostModel) : NotificationCenterEvent
    data class ChangeColor(val color: Color?) : NotificationCenterEvent
    data class ChangeZombieScrollAmount(val value: Float) : NotificationCenterEvent
    data class MultiCommunityCreated(val model: MultiCommunityModel) : NotificationCenterEvent
    data object CloseDialog : NotificationCenterEvent
    data class SelectCommunity(val model: CommunityModel) : NotificationCenterEvent
    data class PostRemoved(val model: PostModel) : NotificationCenterEvent
    data class CommentRemoved(val model: CommentModel) : NotificationCenterEvent
    data class ChangeReportListType(val unresolvedOnly: Boolean) : NotificationCenterEvent
    data class UserBannedPost(val postId: Int, val user: UserModel) : NotificationCenterEvent
    data class UserBannedComment(val commentId: Int, val user: UserModel) : NotificationCenterEvent
}