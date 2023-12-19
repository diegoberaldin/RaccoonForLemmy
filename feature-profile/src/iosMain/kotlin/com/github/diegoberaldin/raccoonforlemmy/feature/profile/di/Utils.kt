package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getProfileScreenModel(): ProfileMainMviModel = ProfileScreenModelHelper.profileModel

object ProfileScreenModelHelper : KoinComponent {
    val profileModel: ProfileMainMviModel by inject()
}
