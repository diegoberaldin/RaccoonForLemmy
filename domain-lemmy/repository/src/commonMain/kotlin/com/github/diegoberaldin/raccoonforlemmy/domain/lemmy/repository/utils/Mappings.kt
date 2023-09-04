package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentReplyView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentSortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Community
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.All
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.Local
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType.Subscribed
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Person
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonAggregates
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonMentionView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostView
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Active
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Hot
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.MostComments
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.New
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.NewComments
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Old
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.Top
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopDay
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopMonth
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopSixHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopTwelveHour
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopWeek
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType.TopYear
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
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
    else -> Active
}

internal fun SortType.toCommentDto(): CommentSortType = when (this) {
    SortType.Hot -> CommentSortType.Hot
    SortType.New -> CommentSortType.New
    SortType.Top.Generic -> CommentSortType.Top
    SortType.Old -> CommentSortType.Old
    else -> CommentSortType.New
}

internal fun Person.toModel() = UserModel(
    id = id,
    name = name,
    avatar = avatar,
    host = actorId.toHost(),
    accountAge = published,
)

internal fun PersonAggregates.toModel() = UserScoreModel(
    postScore = postScore,
    commentScore = commentScore,
)

internal fun PostView.toModel() = PostModel(
    id = post.id,
    title = post.name,
    text = post.body.orEmpty(),
    score = counts.score,
    comments = counts.comments,
    thumbnailUrl = post.thumbnailUrl.orEmpty(),
    community = community.toModel(),
    creator = creator.toModel(),
    saved = saved,
    myVote = myVote ?: 0,
    publishDate = post.published,
    nsfw = post.nsfw,
)

internal fun CommentView.toModel() = CommentModel(
    id = comment.id,
    text = comment.content,
    community = community.toModel(),
    creator = creator.toModel(),
    score = counts.score,
    saved = saved,
    myVote = myVote ?: 0,
    publishDate = comment.published,
    postId = comment.postId,
    comments = counts.childCount,
)

internal fun Community.toModel() = CommunityModel(
    id = id,
    name = name,
    title = title,
    description = description.orEmpty(),
    icon = icon,
    banner = banner,
    instanceUrl = actorId.communityToInstanceUrl(),
    host = actorId.toHost(),
    nsfw = nsfw,
)

internal fun PersonMentionView.toModel() = PersonMentionModel(
    id = personMention.id,
    post = PostModel(
        id = post.id,
        title = post.name,
        text = post.body.orEmpty(),
        nsfw = post.nsfw,
    ),
    comment = CommentModel(
        id = comment.id,
        postId = comment.postId,
        text = comment.content,
        community = community.toModel(),
    ),
    creator = creator.toModel(),
    community = community.toModel(),
    score = counts.score,
    myVote = myVote ?: 0,
    saved = saved,
    publishDate = personMention.published,
)

internal fun CommentReplyView.toModel() = PersonMentionModel(
    id = commentReply.id,
    post = PostModel(
        id = post.id,
        title = post.name,
        text = post.body.orEmpty(),
        creator = UserModel(id = post.creatorId),
        publishDate = post.published,
        nsfw = post.nsfw,
    ),
    comment = CommentModel(
        id = comment.id,
        postId = comment.postId,
        text = comment.content,
        community = community.toModel(),
        publishDate = comment.published,
    ),
    creator = creator.toModel(),
    community = community.toModel(),
    score = counts.score,
    myVote = myVote ?: 0,
    saved = saved,
    publishDate = commentReply.published,
)

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
