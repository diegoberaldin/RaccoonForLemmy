package com.github.diegoberaldin.raccoonforlemmy.unit.messages.di

import com.github.diegoberaldin.raccoonforlemmy.unit.messages.InboxMessagesMviModel
import org.koin.java.KoinJavaComponent

actual fun getInboxMessagesViewModel(): InboxMessagesMviModel {
    val res: InboxMessagesMviModel by KoinJavaComponent.inject(InboxMessagesMviModel::class.java)
    return res
}
