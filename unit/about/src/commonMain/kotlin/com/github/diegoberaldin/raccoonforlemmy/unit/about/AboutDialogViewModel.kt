package com.github.diegoberaldin.raccoonforlemmy.unit.about

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo


class AboutDialogViewModel(
    private val mvi: DefaultMviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect>,
) : AboutDialogMviModel,
    MviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(
                version = AppInfo.versionCode,
            )
        }
    }
}
