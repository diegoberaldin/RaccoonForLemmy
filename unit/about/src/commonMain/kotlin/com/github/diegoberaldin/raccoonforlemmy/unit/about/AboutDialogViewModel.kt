package com.github.diegoberaldin.raccoonforlemmy.unit.about

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo


class AboutDialogViewModel : AboutDialogMviModel,
    DefaultMviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect>(
        initialState = AboutDialogMviModel.UiState(),
    ) {

    init {
        updateState {
            it.copy(
                version = AppInfo.versionCode,
            )
        }
    }
}
