package com.github.diegoberaldin.raccoonforlemmy.unit.replies.di

import com.github.diegoberaldin.raccoonforlemmy.unit.replies.InboxRepliesMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxRepliesViewModel(): InboxRepliesMviModel = InboxRepliesDiHelper.repliesModel

object InboxRepliesDiHelper : KoinComponent {
    val repliesModel: InboxRepliesMviModel by inject()
}
