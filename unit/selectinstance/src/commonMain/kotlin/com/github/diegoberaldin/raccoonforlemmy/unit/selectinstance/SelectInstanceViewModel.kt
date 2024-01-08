package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

private const val CUSTOM_INTANCES_KEY = "customInstances"
private val DEFAULT_INSTANCES = listOf(
    "lemmy.world",
    "lemmy.ml",
    "lemmy.dbzer0.com",
    "sh.itjust.works",
    "lemm.ee",
    "feddit.de",
    "programming.dev",
    "discuss.tchncs.de",
    "sopuli.xyz",
    "lemmy.blahaj.zone",
    "lemmy.zip",
    "reddthat.com",
    "lemmy.cafe",
)

class SelectInstanceViewModel(
    private val mvi: DefaultMviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect>,
    private val keyStore: TemporaryKeyStore,
) : SelectInstanceMviModel,
    MviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        if (uiState.value.instances.isEmpty()) {
            if (!keyStore.containsKey(CUSTOM_INTANCES_KEY)) {
                keyStore.save(
                    key = CUSTOM_INTANCES_KEY,
                    value = DEFAULT_INSTANCES,
                )
            }
            val instances = keyStore.get(key = CUSTOM_INTANCES_KEY, default = DEFAULT_INSTANCES)
            mvi.updateState { it.copy(instances = instances) }
        }
    }
}