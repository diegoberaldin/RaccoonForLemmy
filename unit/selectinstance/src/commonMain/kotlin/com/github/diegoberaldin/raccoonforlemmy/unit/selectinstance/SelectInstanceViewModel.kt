package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.InstanceSelectionRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SelectInstanceViewModel(
    private val mvi: DefaultMviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect>,
    private val instanceRepository: InstanceSelectionRepository,
    private val communityRepository: CommunityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
) : SelectInstanceMviModel,
    MviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            apiConfigurationRepository.instance.onEach { instance ->
                mvi.updateState { it.copy(currentInstance = instance) }
            }.launchIn(this)
        }

        if (uiState.value.instances.isEmpty()) {
            mvi.scope?.launch(Dispatchers.IO) {
                val instances = instanceRepository.getAll()
                mvi.updateState { it.copy(instances = instances) }
            }
        }
    }

    override fun reduce(intent: SelectInstanceMviModel.Intent) {
        when (intent) {
            is SelectInstanceMviModel.Intent.SelectInstance -> {
                confirmSelection(intent.value)
            }

            is SelectInstanceMviModel.Intent.ChangeInstanceName -> {
                mvi.updateState { it.copy(changeInstanceName = intent.value) }
            }

            is SelectInstanceMviModel.Intent.SubmitChangeInstanceDialog -> submitChangeInstance()
            is SelectInstanceMviModel.Intent.DeleteInstance -> deleteInstance(intent.value)
        }
    }

    private fun deleteInstance(value: String) {
        mvi.scope?.launch(Dispatchers.IO) {
            instanceRepository.remove(value)
            val instances = instanceRepository.getAll()
            mvi.updateState { it.copy(instances = instances) }
        }
    }

    private fun submitChangeInstance() {
        mvi.updateState { it.copy(changeInstanceNameError = null) }
        var valid = true
        val instanceName = uiState.value.changeInstanceName
        if (instanceName.isEmpty()) {
            mvi.updateState { it.copy(changeInstanceNameError = MR.strings.message_missing_field.desc()) }
            valid = false
        }
        if (!valid) {
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(changeInstanceLoading = true) }
            val res = communityRepository.getAll(
                instance = instanceName,
                page = 1,
                limit = 1
            ) ?: emptyList()
            if (res.isEmpty()) {
                mvi.updateState {
                    it.copy(
                        changeInstanceNameError = MR.strings.message_invalid_field.desc(),
                        changeInstanceLoading = false,
                    )
                }
                return@launch
            }

            mvi.updateState {
                it.copy(
                    changeInstanceLoading = false,
                    changeInstanceName = "",
                )
            }
            instanceRepository.add(instanceName)

            mvi.emitEffect(SelectInstanceMviModel.Effect.CloseDialog)
            val instances = instanceRepository.getAll()
            mvi.updateState { it.copy(instances = instances) }
            confirmSelection(instanceName)
        }
    }

    private fun confirmSelection(value: String) {
        apiConfigurationRepository.changeInstance(value)
        mvi.scope?.launch {
            mvi.emitEffect(SelectInstanceMviModel.Effect.Confirm(value))
        }
    }
}