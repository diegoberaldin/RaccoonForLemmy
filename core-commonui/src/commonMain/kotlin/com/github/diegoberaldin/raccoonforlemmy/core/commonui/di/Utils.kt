package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel


expect fun getUserDetailViewModel(
    user: UserModel,
    otherInstance: String = "",
): UserDetailMviModel
