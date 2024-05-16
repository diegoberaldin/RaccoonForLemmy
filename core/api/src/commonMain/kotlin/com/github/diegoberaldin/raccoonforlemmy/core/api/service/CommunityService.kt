package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.AddModToCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.AddModToCommunityResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BanFromCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BanFromCommunityResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockCommunityResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FollowCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetCommunityResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.HideCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListCommunitiesResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgeCommunityForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SuccessResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query

interface CommunityService {
    @GET("community")
    suspend fun get(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("id") id: CommunityId? = null,
        @Query("name") name: String? = null,
    ): Response<GetCommunityResponse>

    @GET("community/list")
    suspend fun getAll(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("show_nsfw") showNsfw: Boolean = true,
        @Query("sort") sort: SortType = SortType.Active,
    ): Response<ListCommunitiesResponse>

    @POST("community/follow")
    @Headers("Content-Type: application/json")
    suspend fun follow(
        @Header("Authorization") authHeader: String? = null,
        @Body form: FollowCommunityForm,
    ): Response<CommunityResponse>

    @POST("community/block")
    @Headers("Content-Type: application/json")
    suspend fun block(
        @Header("Authorization") authHeader: String? = null,
        @Body form: BlockCommunityForm,
    ): Response<BlockCommunityResponse>

    @POST("community/ban_user")
    @Headers("Content-Type: application/json")
    suspend fun ban(
        @Header("Authorization") authHeader: String? = null,
        @Body form: BanFromCommunityForm,
    ): Response<BanFromCommunityResponse>

    @POST("community/mod")
    @Headers("Content-Type: application/json")
    suspend fun addMod(
        @Header("Authorization") authHeader: String? = null,
        @Body form: AddModToCommunityForm,
    ): Response<AddModToCommunityResponse>

    @PUT("community")
    @Headers("Content-Type: application/json")
    suspend fun edit(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditCommunityForm,
    ): Response<CommunityResponse>

    @PUT("community/hide")
    @Headers("Content-Type: application/json")
    suspend fun hide(
        @Header("Authorization") authHeader: String? = null,
        @Body form: HideCommunityForm,
    ): Response<SuccessResponse>

    @POST("admin/purge/community")
    @Headers("Content-Type: application/json")
    suspend fun purge(
        @Header("Authorization") authHeader: String? = null,
        @Body form: PurgeCommunityForm,
    ): Response<SuccessResponse>
}
