package com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getDetailOpener(): DetailOpener = DetailOpenerApiDiHelper.detailOpener

object DetailOpenerApiDiHelper : KoinComponent {
    val detailOpener: DetailOpener by inject()
}
