package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class SubscribedType {
    @SerialName("Subscribed")
    Subscribed,

    @SerialName("NotSubscribed")
    NotSubscribed,

    @SerialName("Pending")
    Pending,
}
