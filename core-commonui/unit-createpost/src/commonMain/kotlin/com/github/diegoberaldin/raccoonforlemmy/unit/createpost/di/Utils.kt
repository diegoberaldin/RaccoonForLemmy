package com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di

import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.CreatePostMviModel

expect fun getCreatePostViewModel(
    editedPostId: Int?,
): CreatePostMviModel
