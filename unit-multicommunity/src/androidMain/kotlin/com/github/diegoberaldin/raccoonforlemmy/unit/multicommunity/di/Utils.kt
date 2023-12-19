package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor.MultiCommunityEditorMviModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

actual fun getMultiCommunityViewModel(community: MultiCommunityModel): MultiCommunityMviModel {
    val res: MultiCommunityMviModel by KoinJavaComponent.inject(
        MultiCommunityMviModel::class.java,
        parameters = { parametersOf(community) }
    )
    return res
}

actual fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?): MultiCommunityEditorMviModel {
    val res: MultiCommunityEditorMviModel by KoinJavaComponent.inject(
        MultiCommunityEditorMviModel::class.java,
        parameters = { parametersOf(editedCommunity) }
    )
    return res
}
