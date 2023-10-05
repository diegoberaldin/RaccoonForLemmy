package com.github.diegoberaldin.raccoonforlemmy.feature.search.di

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.main.ExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getExploreViewModel() = SearchScreenModelHelper.model

actual fun getManageSubscriptionsViewModel() = SearchScreenModelHelper.manageSuscriptionsViewModel

actual fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?) =
    SearchScreenModelHelper.getMultiCommunityEditorViewModel(editedCommunity)

object SearchScreenModelHelper : KoinComponent {
    val model: ExploreViewModel by inject()
    val manageSuscriptionsViewModel: ManageSubscriptionsViewModel by inject()

    internal fun getMultiCommunityEditorViewModel(editedCommunity: MultiCommunityModel?): MultiCommunityEditorViewModel {
        val res: MultiCommunityEditorViewModel by inject(parameters = { parametersOf(editedCommunity) })
        return res
    }
}
