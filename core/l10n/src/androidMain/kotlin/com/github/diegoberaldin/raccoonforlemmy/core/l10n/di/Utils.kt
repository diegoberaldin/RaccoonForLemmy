package com.github.diegoberaldin.raccoonforlemmy.core.l10n.di

import com.github.diegoberaldin.raccoonforlemmy.core.l10n.L10nManager
import org.koin.java.KoinJavaComponent

actual fun getL10nManager(): L10nManager {
    val res: L10nManager by KoinJavaComponent.inject(L10nManager::class.java)
    return res
}
