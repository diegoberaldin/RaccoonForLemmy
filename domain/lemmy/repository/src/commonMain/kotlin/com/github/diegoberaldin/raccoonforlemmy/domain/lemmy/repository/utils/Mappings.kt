package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Comment
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentReplyView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentReportView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentSortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Community
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.All
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.Local
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.Subscribed
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModAddCommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModBanFromCommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModFeaturePostView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModLockPostView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModRemoveCommentView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModRemovePostView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModTransferCommunityView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModlogActionType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Person
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonAggregates
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonMentionView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Post
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostReportView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PrivateMessageView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SearchType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SiteMetadata
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Active
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Controversial
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Hot
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.MostComments
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.New
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.NewComments
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Old
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Scaled
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Top
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopDay
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopMonth
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopSixHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopTwelveHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopWeek
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopYear
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SubscribedType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItemType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserScoreModel

internal fun ListingType.toDto() = when (this) {
    ListingType.All -> All
    ListingType.Subscribed -> Subscribed
    ListingType.Local -> Local
}

internal fun SortType.toDto() = when (this) {
    SortType.Hot -> Hot
    SortType.MostComments -> MostComments
    SortType.New -> New
    SortType.NewComments -> NewComments
    SortType.Top.Day -> TopDay
    SortType.Top.Month -> TopMonth
    SortType.Top.Past12Hours -> TopTwelveHour
    SortType.Top.Past6Hours -> TopSixHour
    SortType.Top.PastHour -> TopHour
    SortType.Top.Week -> TopWeek
    SortType.Top.Year -> TopYear
    SortType.Top.Generic -> Top
    SortType.Old -> Old
    SortType.Scaled -> Scaled
    SortType.Controversial -> Controversial
    else -> Active
}

internal fun SortType.toCommentDto(): CommentSortType = when (this) {
    SortType.Hot -> CommentSortType.Hot
    SortType.New -> CommentSortType.New
    SortType.Top.Generic -> CommentSortType.Top
    SortType.Old -> CommentSortType.Old
    else -> CommentSortType.New
}

internal fun SearchResultType.toDto(): SearchType = when (this) {
    SearchResultType.All -> SearchType.All
    SearchResultType.Comments -> SearchType.Comments
    SearchResultType.Communities -> SearchType.Communities
    SearchResultType.Posts -> SearchType.Posts
    SearchResultType.Users -> SearchType.Users
}

internal fun Person.toModel() = UserModel(
    id = id,
    instanceId = instanceId,
    name = name,
    displayName = displayName.orEmpty(),
    avatar = avatar,
    host = actorId.toHost(),
    accountAge = published,
    banned = banned,
    bio = bio,
    updateDate = updated,
    admin = admin ?: false,
)

internal fun PersonView.toModel() = person.toModel()

internal fun PersonAggregates.toModel() = UserScoreModel(
    postScore = postScore ?: 0,
    commentScore = commentScore ?: 0,
)

internal fun PostView.toModel() = PostModel(
    id = post.id,
    originalUrl = post.apId,
    title = post.name,
    text = post.body.orEmpty(),
    score = counts.score,
    upvotes = counts.upvotes,
    downvotes = counts.downvotes,
    comments = counts.comments,
    thumbnailUrl = post.thumbnailUrl.orEmpty(),
    url = post.url,
    community = community.toModel(),
    creator = creator.toModel(),
    saved = saved,
    myVote = myVote ?: 0,
    publishDate = post.published,
    updateDate = post.updated,
    nsfw = post.nsfw,
    embedVideoUrl = post.embedVideoUrl,
    read = read,
    featuredCommunity = post.featuredCommunity,
    removed = post.removed,
    locked = post.locked,
)

internal fun Post.toModel() = PostModel(
    id = id,
    originalUrl = apId,
    title = name,
    text = body.orEmpty(),
    thumbnailUrl = thumbnailUrl.orEmpty(),
    url = url,
    updateDate = updated,
    nsfw = nsfw,
    embedVideoUrl = embedVideoUrl,
    featuredCommunity = featuredCommunity,
    removed = removed,
    locked = locked,
)

internal fun CommentView.toModel() = CommentModel(
    id = comment.id,
    text = comment.content,
    community = community.toModel(),
    creator = creator.toModel(),
    score = counts.score,
    upvotes = counts.upvotes,
    downvotes = counts.downvotes,
    saved = saved,
    myVote = myVote ?: 0,
    publishDate = comment.published,
    updateDate = comment.updated,
    postId = comment.postId,
    comments = counts.childCount,
    path = comment.path,
    distinguished = comment.distinguished,
    removed = comment.removed,
)

internal fun Comment.toModel() = CommentModel(
    id = id,
    text = content,
    publishDate = published,
    updateDate = updated,
    postId = postId,
    path = path,
    distinguished = distinguished,
    removed = removed,
)

internal fun Community.toModel() = CommunityModel(
    id = id,
    instanceId = instanceId,
    name = name,
    title = title,
    description = description.orEmpty(),
    icon = icon,
    banner = banner,
    instanceUrl = actorId.communityToInstanceUrl(),
    host = actorId.toHost(),
    nsfw = nsfw,
    creationDate = published,
)

internal fun CommunityView.toModel() = community.toModel().copy(
    monthlyActiveUsers = counts.usersActiveMonth,
    weeklyActiveUsers = counts.usersActiveWeek,
    dailyActiveUsers = counts.usersActiveDay,
    subscribed = subscribed == SubscribedType.Subscribed,
    subscribers = counts.subscribers,
    posts = counts.posts,
    comments = counts.comments,
)

internal fun PersonMentionView.toModel() = PersonMentionModel(
    id = personMention.id,
    read = personMention.read,
    post = PostModel(
        id = post.id,
        originalUrl = post.apId,
        title = post.name,
        text = post.body.orEmpty(),
        score = counts.score,
        upvotes = counts.upvotes,
        downvotes = counts.downvotes,
        thumbnailUrl = post.thumbnailUrl.orEmpty(),
        url = post.url,
        community = community.toModel(),
        creator = creator.toModel(),
        saved = saved,
        myVote = myVote ?: 0,
        publishDate = post.published,
        updateDate = post.updated,
        nsfw = post.nsfw,
        embedVideoUrl = post.embedVideoUrl,
        featuredCommunity = post.featuredCommunity,
        removed = post.removed,
        locked = post.locked,
    ),
    comment = CommentModel(
        id = comment.id,
        postId = comment.postId,
        text = comment.content,
        community = community.toModel(),
        publishDate = comment.published,
        updateDate = comment.updated,
        distinguished = comment.distinguished,
        removed = comment.removed,
    ),
    creator = creator.toModel(),
    community = community.toModel(),
    score = counts.score,
    myVote = myVote ?: 0,
    saved = saved,
    publishDate = personMention.published,
    upvotes = counts.upvotes,
    downvotes = counts.downvotes,
)

internal fun CommentReplyView.toModel() = PersonMentionModel(
    id = commentReply.id,
    read = commentReply.read,
    post = PostModel(
        id = post.id,
        originalUrl = post.apId,
        title = post.name,
        text = post.body.orEmpty(),
        score = counts.score,
        upvotes = counts.upvotes,
        downvotes = counts.downvotes,
        thumbnailUrl = post.thumbnailUrl.orEmpty(),
        url = post.url,
        community = community.toModel(),
        creator = UserModel(id = post.creatorId),
        saved = saved,
        myVote = myVote ?: 0,
        publishDate = post.published,
        updateDate = post.updated,
        nsfw = post.nsfw,
        embedVideoUrl = post.embedVideoUrl,
        featuredCommunity = post.featuredCommunity,
        removed = post.removed,
        locked = post.locked,
    ),
    comment = CommentModel(
        id = comment.id,
        postId = comment.postId,
        text = comment.content,
        community = community.toModel(),
        publishDate = comment.published,
        updateDate = comment.updated,
        distinguished = comment.distinguished,
        removed = comment.removed,
    ),
    creator = creator.toModel(),
    community = community.toModel(),
    score = counts.score,
    myVote = myVote ?: 0,
    saved = saved,
    publishDate = commentReply.published,
    upvotes = counts.upvotes,
    downvotes = counts.downvotes,
)

internal fun PrivateMessageView.toModel() = PrivateMessageModel(
    id = privateMessage.id,
    content = privateMessage.content,
    creator = creator.toModel(),
    recipient = recipient.toModel(),
    publishDate = privateMessage.published,
    updateDate = privateMessage.updated,
    read = privateMessage.read,
)

internal fun String?.toAuthHeader() = this?.let { "Bearer $it" }

internal fun String.toHost(): String = this.replace("https://", "").let {
    val index = it.indexOf("/")
    if (index < 0) {
        return this
    }
    it.substring(0, index)
}

private fun String.communityToInstanceUrl(): String {
    val index = this.indexOf("/c/")
    if (index < 0) {
        return this
    }
    return this.substring(0, index)
}

internal fun PostReportView.toModel() = PostReportModel(
    id = postReport.id,
    postId = post.id,
    reason = postReport.reason,
    creator = creator.toModel(),
    publishDate = postReport.published,
    resolved = postReport.resolved,
    resolver = resolver?.toModel(),
    originalText = postReport.originalPostBody,
    originalTitle = postReport.originalPostName,
    originalUrl = postReport.originalPostUrl,
    thumbnailUrl = post.thumbnailUrl,
    updateDate = postReport.updated,
)

internal fun CommentReportView.toModel() = CommentReportModel(
    id = commentReport.id,
    postId = comment.postId,
    commentId = comment.id,
    reason = commentReport.reason,
    creator = creator.toModel(),
    publishDate = commentReport.published,
    resolved = commentReport.resolved,
    resolver = resolver?.toModel(),
    originalText = commentReport.originalCommentText,
    updateDate = commentReport.updated,
)

internal fun SiteMetadata.toModel() = MetadataModel(
    title = title.orEmpty(),
    description = description.orEmpty(),
)

internal fun ModlogItemType.toDto(): ModlogActionType = when (this) {
    ModlogItemType.All -> ModlogActionType.All
    ModlogItemType.ModRemovePost -> ModlogActionType.ModRemovePost
    ModlogItemType.ModLockPost -> ModlogActionType.ModLockPost
    ModlogItemType.ModAdd -> ModlogActionType.ModAddCommunity
    ModlogItemType.ModBanFromCommunity -> ModlogActionType.ModBanFromCommunity
    ModlogItemType.ModFeaturePost -> ModlogActionType.ModFeaturePost
    ModlogItemType.ModRemoveComment -> ModlogActionType.ModRemoveComment
    ModlogItemType.ModTransferCommunity -> ModlogActionType.ModTransferCommunity
}

internal fun ModAddCommunityView.toDto() = ModlogItem.ModAdd(
    id = modAddCommunity.id,
    date = modAddCommunity.date,
    removed = modAddCommunity.removed,
    user = moddedPerson.toModel(),
    moderator = moderator?.toModel(),
)

internal fun ModBanFromCommunityView.toDto() = ModlogItem.ModBanFromCommunity(
    id = modBanFromCommunity.id,
    date = modBanFromCommunity.date,
    banned = modBanFromCommunity.banned,
    user = bannedPerson.toModel(),
    moderator = moderator?.toModel(),
)

internal fun ModFeaturePostView.toDto() = ModlogItem.ModFeaturePost(
    id = modFeaturePost.id,
    date = modFeaturePost.date,
    featured = modFeaturePost.featured,
    moderator = moderator?.toModel(),
    post = post.toModel(),
)

internal fun ModLockPostView.toDto() = ModlogItem.ModLockPost(
    id = modLockPost.id,
    date = modLockPost.date,
    locked = modLockPost.locked,
    moderator = moderator?.toModel(),
    post = post.toModel(),
)

internal fun ModRemovePostView.toDto() = ModlogItem.ModRemovePost(
    id = modRemovePost.id,
    date = modRemovePost.date,
    removed = modRemovePost.removed,
    moderator = moderator?.toModel(),
    post = post.toModel(),
)

internal fun ModRemoveCommentView.toDto() = ModlogItem.ModRemoveComment(
    id = modRemoveComment.id,
    date = modRemoveComment.date,
    removed = modRemoveComment.removed,
    moderator = moderator?.toModel(),
    comment = comment.toModel(),
    post = post.toModel(),
)

internal fun ModTransferCommunityView.toDto() = ModlogItem.ModTransferCommunity(
    id = modTransferCommunity.id,
    date = modTransferCommunity.date,
    moderator = moderator?.toModel(),
    user = moddedPerson.toModel(),
)
