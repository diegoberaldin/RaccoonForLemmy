package com.github.diegoberaldin.raccoonforlemmy.core.l10n.di

import com.github.diegoberaldin.raccoonforlemmy.core.l10n.L10nManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getL10nManager(): L10nManager = CoreL10nKoinHelper.l10nManager

object CoreL10nKoinHelper : KoinComponent {
    val l10nManager: L10nManager by inject()
}
