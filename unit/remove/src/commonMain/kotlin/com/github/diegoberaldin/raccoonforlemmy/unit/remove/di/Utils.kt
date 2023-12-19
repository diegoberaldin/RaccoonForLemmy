package com.github.diegoberaldin.raccoonforlemmy.unit.remove.di

import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveMviModel

expect fun getRemoveViewModel(
    postId: Int? = null,
    commentId: Int? = null,
): RemoveMviModel
