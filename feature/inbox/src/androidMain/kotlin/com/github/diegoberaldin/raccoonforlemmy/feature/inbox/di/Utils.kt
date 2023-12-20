package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getInboxViewModel(): InboxMviModel {
    val res: InboxMviModel by inject(InboxMviModel::class.java)
    return res
}
