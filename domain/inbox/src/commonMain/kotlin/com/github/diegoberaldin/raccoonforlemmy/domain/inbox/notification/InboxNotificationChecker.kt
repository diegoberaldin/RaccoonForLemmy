package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification

interface InboxNotificationChecker {

    val isBackgroundCheckSupported: Boolean

    fun setPeriod(minutes: Long)

    fun start()

    fun stop()
}
