package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.UserDetailMviModel

expect fun getUserDetailViewModel(
    user: UserModel,
    otherInstance: String = "",
): UserDetailMviModel
