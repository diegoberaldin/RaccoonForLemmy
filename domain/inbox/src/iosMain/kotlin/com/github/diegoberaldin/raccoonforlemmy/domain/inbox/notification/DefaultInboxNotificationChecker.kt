package com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification

class DefaultInboxNotificationChecker : InboxNotificationChecker {
    override val isBackgroundCheckSupported = false

    override fun setPeriod(minutes: Long) {
        // NO-OP
    }

    override fun start() {
        // NO-OP
    }

    override fun stop() {
        // NO-OP
    }
}
