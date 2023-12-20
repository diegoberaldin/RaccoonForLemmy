package com.github.diegoberaldin.raccoonforlemmy.unit.mentions.di

import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxMentionsViewModel(): InboxMentionsMviModel = UnitMentionsDiHelper.mentionsModel

object UnitMentionsDiHelper : KoinComponent {
    val mentionsModel: InboxMentionsMviModel by inject()
}
