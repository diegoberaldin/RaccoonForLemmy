package com.github.diegoberaldin.raccoonforlemmy.core.utils.url

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultCustomTabsHelper : CustomTabsHelper {
    override val isSupported = false

    override fun handle(
        url: String,
        noHistory: Boolean,
    ) {
        // no-op
    }
}

actual fun getCustomTabsHelper(): CustomTabsHelper = CustomTabsDiHelper.helper

object CustomTabsDiHelper : KoinComponent {
    val helper: CustomTabsHelper by inject()
}
