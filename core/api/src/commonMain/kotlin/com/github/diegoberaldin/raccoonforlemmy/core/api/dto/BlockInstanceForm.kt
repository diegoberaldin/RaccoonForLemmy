package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockInstanceForm(
    @SerialName("instance_id") val instanceId: InstanceId,
    @SerialName("block") val block: Boolean,
)
