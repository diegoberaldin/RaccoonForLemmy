package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.InstanceSelectionRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectInstanceViewModel(
    private val instanceRepository: InstanceSelectionRepository,
    private val communityRepository: CommunityRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
) : SelectInstanceMviModel,
    DefaultMviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect>(
        initialState = SelectInstanceMviModel.State(),
    ) {

    private val saveOperationChannel = Channel<List<String>>()

    init {
        screenModelScope.launch {
            apiConfigurationRepository.instance.onEach { instance ->
                updateState { it.copy(currentInstance = instance) }
            }.launchIn(this)

            @OptIn(FlowPreview::class)
            saveOperationChannel.receiveAsFlow().debounce(500).onEach { newInstances ->
                instanceRepository.updateAll(newInstances)
            }.launchIn(this)
        }

        if (uiState.value.instances.isEmpty()) {
            screenModelScope.launch(Dispatchers.IO) {
                val instances = instanceRepository.getAll()
                updateState { it.copy(instances = instances) }
            }
        }
    }

    override fun reduce(intent: SelectInstanceMviModel.Intent) {
        when (intent) {
            is SelectInstanceMviModel.Intent.SelectInstance -> {
                confirmSelection(intent.value)
            }

            is SelectInstanceMviModel.Intent.ChangeInstanceName -> {
                updateState { it.copy(changeInstanceName = intent.value) }
            }

            is SelectInstanceMviModel.Intent.SubmitChangeInstanceDialog -> submitChangeInstance()
            is SelectInstanceMviModel.Intent.DeleteInstance -> deleteInstance(intent.value)
            is SelectInstanceMviModel.Intent.SwapIntances -> swapInstances(intent.from, intent.to)
        }
    }

    private fun deleteInstance(value: String) {
        screenModelScope.launch(Dispatchers.IO) {
            instanceRepository.remove(value)
            val instances = instanceRepository.getAll()
            updateState { it.copy(instances = instances) }
        }
    }

    private fun submitChangeInstance() {
        updateState { it.copy(changeInstanceNameError = null) }
        var valid = true
        val instanceName = uiState.value.changeInstanceName
        if (instanceName.isEmpty()) {
            updateState { it.copy(changeInstanceNameError = ValidationError.MissingField) }
            valid = false
        }
        if (!valid) {
            return
        }

        screenModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(changeInstanceLoading = true) }
            val res = communityRepository.getAll(
                instance = instanceName,
                page = 1,
                limit = 1
            ) ?: emptyList()
            if (res.isEmpty()) {
                updateState {
                    it.copy(
                        changeInstanceNameError = ValidationError.InvalidField,
                        changeInstanceLoading = false,
                    )
                }
                return@launch
            }

            updateState {
                it.copy(
                    changeInstanceLoading = false,
                    changeInstanceName = "",
                )
            }
            instanceRepository.add(instanceName)

            emitEffect(SelectInstanceMviModel.Effect.CloseDialog)
            val instances = instanceRepository.getAll()
            updateState { it.copy(instances = instances) }
            confirmSelection(instanceName)
        }
    }

    private fun swapInstances(from: Int, to: Int) {
        val newInstances = uiState.value.instances.toMutableList().apply {
            val element = removeAt(from)
            add(to, element)
        }
        screenModelScope.launch {
            saveOperationChannel.send(newInstances)
            updateState {
                it.copy(instances = newInstances)
            }
        }
    }

    private fun confirmSelection(value: String) {
        apiConfigurationRepository.changeInstance(value)
        screenModelScope.launch {
            emitEffect(SelectInstanceMviModel.Effect.Confirm(value))
        }
    }
}