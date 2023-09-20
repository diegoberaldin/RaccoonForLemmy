package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

interface CreatePostMviModel :
    MviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect> {

    sealed interface Intent {
        data class SetTitle(val value: String) : Intent
        data class SetText(val value: String) : Intent
        data class SetUrl(val value: String) : Intent
        data class ChangeNsfw(val value: Boolean) : Intent
        data class ImageSelected(val value: ByteArray) : Intent

        object Send : Intent
    }

    data class UiState(
        val title: String = "",
        val titleError: StringDesc? = null,
        val body: String = "",
        val bodyError: StringDesc? = null,
        val url: String = "",
        val urlError: StringDesc? = null,
        val nsfw: Boolean = false,
        val loading: Boolean = false,
    )

    sealed interface Effect {
        object Success : Effect

        data class Failure(val message: String?) : Effect
    }
}
