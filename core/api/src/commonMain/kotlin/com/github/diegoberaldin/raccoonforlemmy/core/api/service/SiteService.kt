package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockSiteForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetSiteMetadataResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetSiteResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface SiteService {
    @GET("site")
    suspend fun get(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
    ): Response<GetSiteResponse>

    @POST("site/block")
    @Headers("Content-Type: application/json")
    suspend fun block(
        @Header("Authorization") authHeader: String? = null,
        @Body form: BlockSiteForm,
    ): Response<GetSiteResponse>

    @GET("post/site_metadata")
    suspend fun getSiteMetadata(
        @Header("Authorization") authHeader: String? = null,
        @Query("url")
        url: String,
    ): Response<GetSiteMetadataResponse>
}
