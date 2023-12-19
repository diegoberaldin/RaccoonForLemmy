package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor.MultiCommunityEditorMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getMultiCommunityViewModel(community: MultiCommunityModel): MultiCommunityMviModel =
    UnitMultiCommunityDiHelper.getMultiCommunityViewModel(community)

actual fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?): MultiCommunityEditorMviModel =
    UnitMultiCommunityDiHelper.getMultiCommunityEditorViewModel(editedCommunity)

object UnitMultiCommunityDiHelper : KoinComponent {
    internal fun getMultiCommunityViewModel(community: MultiCommunityModel): MultiCommunityMviModel {
        val res: MultiCommunityMviModel by inject(parameters = { parametersOf(community) })
        return res
    }

    internal fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?): MultiCommunityEditorMviModel {
        val res: MultiCommunityEditorMviModel by inject(parameters = { parametersOf(editedCommunity) })
        return res
    }
}