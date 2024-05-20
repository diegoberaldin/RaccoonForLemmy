package com.github.diegoberaldin.raccoonforlemmy.unit.about

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo
import kotlinx.coroutines.launch

class AboutDialogViewModel : AboutDialogMviModel,
    DefaultMviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect>(
        initialState = AboutDialogMviModel.UiState(),
    ) {
    init {
        screenModelScope.launch {
            updateState {
                it.copy(
                    version = AppInfo.versionCode,
                )
            }
        }
    }
}
