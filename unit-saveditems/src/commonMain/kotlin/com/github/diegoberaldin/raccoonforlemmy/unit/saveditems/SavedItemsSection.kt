package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems

sealed interface SavedItemsSection {
    data object Posts : SavedItemsSection

    data object Comments : SavedItemsSection
}
