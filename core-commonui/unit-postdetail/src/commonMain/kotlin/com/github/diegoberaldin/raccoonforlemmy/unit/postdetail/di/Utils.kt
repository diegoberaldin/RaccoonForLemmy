package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.PostDetailMviModel

expect fun getPostDetailViewModel(
    post: PostModel,
    otherInstance: String = "",
    highlightCommentId: Int? = null,
    isModerator: Boolean = false,
): PostDetailMviModel
