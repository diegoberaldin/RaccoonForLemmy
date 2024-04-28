package com.github.diegoberaldin.raccoonforlemmy.core.utils.url

interface CustomTabsHelper {

    val isSupported: Boolean

    fun handle(url: String)
}

expect fun getCustomTabsHelper(): CustomTabsHelper
