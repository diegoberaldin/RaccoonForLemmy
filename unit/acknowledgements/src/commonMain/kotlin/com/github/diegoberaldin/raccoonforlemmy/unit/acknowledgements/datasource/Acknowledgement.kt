package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource

import kotlinx.serialization.Serializable

@Serializable
data class Acknowledgement(
    val title: String? = null,
    val subtitle: String? = null,
    val avatar: String? = null,
    val url: String? = null,
)
