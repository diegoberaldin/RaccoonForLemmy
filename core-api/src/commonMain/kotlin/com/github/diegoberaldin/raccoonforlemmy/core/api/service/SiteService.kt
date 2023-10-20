package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetSiteMetadataResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetSiteResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query

interface SiteService {
    @GET("site")
    suspend fun get(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
    ): Response<GetSiteResponse>

    @GET("post/site_metadata")
    suspend fun getSiteMetadata(
        @Header("Authorization") authHeader: String? = null,
        @Query("url")
        url: String,
    ): Response<GetSiteMetadataResponse>
}
