package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getProfileScreenModel(): ProfileMainMviModel {
    val res: ProfileMainMviModel by inject(ProfileMainMviModel::class.java)
    return res
}
