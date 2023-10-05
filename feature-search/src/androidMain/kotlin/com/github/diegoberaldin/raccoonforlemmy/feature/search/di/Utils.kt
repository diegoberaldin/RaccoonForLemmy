package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorViewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getExploreViewModel(): ExploreViewModel {
    val res: ExploreViewModel by inject(ExploreViewModel::class.java)
    return res
}

actual fun getManageSubscriptionsViewModel(): ManageSubscriptionsViewModel {
    val res: ManageSubscriptionsViewModel by inject(ManageSubscriptionsViewModel::class.java)
    return res
}

actual fun getMultiCommunityViewModel(community: MultiCommunityModel): MultiCommunityViewModel {
    val res: MultiCommunityViewModel by inject(
        MultiCommunityViewModel::class.java,
        parameters = { parametersOf(community) }
    )
    return res
}

actual fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?): MultiCommunityEditorViewModel {
    val res: MultiCommunityEditorViewModel by inject(
        MultiCommunityEditorViewModel::class.java,
        parameters = { parametersOf(editedCommunity) }
    )
    return res
}
