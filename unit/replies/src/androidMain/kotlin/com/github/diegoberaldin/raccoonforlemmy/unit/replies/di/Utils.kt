package com.github.diegoberaldin.raccoonforlemmy.unit.replies.di

import com.github.diegoberaldin.raccoonforlemmy.unit.replies.InboxRepliesMviModel
import org.koin.java.KoinJavaComponent

actual fun getInboxRepliesViewModel(): InboxRepliesMviModel {
    val res: InboxRepliesMviModel by KoinJavaComponent.inject(InboxRepliesMviModel::class.java)
    return res
}
