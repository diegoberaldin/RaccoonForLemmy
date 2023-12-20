package com.github.diegoberaldin.raccoonforlemmy.unit.mentions.di

import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsMviModel
import org.koin.java.KoinJavaComponent

actual fun getInboxMentionsViewModel(): InboxMentionsMviModel {
    val res: InboxMentionsMviModel by KoinJavaComponent.inject(InboxMentionsMviModel::class.java)
    return res
}
