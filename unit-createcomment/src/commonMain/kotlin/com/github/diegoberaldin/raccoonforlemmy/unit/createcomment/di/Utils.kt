package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.CreateCommentMviModel

expect fun getCreateCommentViewModel(
    postId: Int? = null,
    parentId: Int? = null,
    editedCommentId: Int? = null,
): CreateCommentMviModel
