package com.github.diegoberaldin.raccoonforlemmy.unit.chat.di

import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getInboxChatViewModel(otherUserId: Int): InboxChatMviModel {
    val res: InboxChatMviModel by KoinJavaComponent.inject(
        InboxChatMviModel::class.java,
        parameters = {
            parametersOf(otherUserId)
        },
    )
    return res
}
