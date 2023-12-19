package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.di

import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedMviModel
import org.koin.java.KoinJavaComponent

actual fun getProfileLoggedViewModel(): ProfileLoggedMviModel {
    val res: ProfileLoggedMviModel by KoinJavaComponent.inject(ProfileLoggedMviModel::class.java)
    return res
}
