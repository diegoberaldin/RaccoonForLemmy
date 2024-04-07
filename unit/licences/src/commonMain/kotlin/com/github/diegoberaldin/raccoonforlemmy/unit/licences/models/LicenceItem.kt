package com.github.diegoberaldin.raccoonforlemmy.unit.licences.models

data class LicenceItem(
    val type: LicenceItemType? = null,
    val title: String = "",
    val subtitle: String = "",
    val url: String = "",
)
