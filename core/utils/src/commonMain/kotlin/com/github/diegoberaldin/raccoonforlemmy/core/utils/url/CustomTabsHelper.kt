package com.github.diegoberaldin.raccoonforlemmy.core.utils.url

interface CustomTabsHelper {
    val isSupported: Boolean

    fun handle(
        url: String,
        noHistory: Boolean = false,
    )
}

expect fun getCustomTabsHelper(): CustomTabsHelper
