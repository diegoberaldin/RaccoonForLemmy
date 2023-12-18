package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.di

import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.SavedItemsMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getSavedItemsViewModel(): SavedItemsMviModel =
    UnitSavedItemsDiHelper.savedItemsViewModel

object UnitSavedItemsDiHelper : KoinComponent {

    val savedItemsViewModel: SavedItemsMviModel by inject()
}