package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SiteView(
    @SerialName("site") val site: Site,
    @SerialName("local_site") val localSite: LocalSite,
    @SerialName("local_site_rate_limit") val localSiteRateLimit: LocalSiteRateLimit?,
    @SerialName("counts") val counts: SiteAggregates,
)
