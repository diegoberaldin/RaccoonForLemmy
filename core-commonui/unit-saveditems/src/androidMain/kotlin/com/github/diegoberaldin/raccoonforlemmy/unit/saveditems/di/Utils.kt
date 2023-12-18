package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.di

import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.SavedItemsMviModel
import org.koin.java.KoinJavaComponent

actual fun getSavedItemsViewModel(): SavedItemsMviModel {
    val res: SavedItemsMviModel by KoinJavaComponent.inject(
        clazz = SavedItemsMviModel::class.java,
    )
    return res
}
