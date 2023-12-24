package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.UserInfoMviModel

expect fun getUserInfoViewModel(user: UserModel): UserInfoMviModel