package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeletePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditPostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.FeaturePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetSiteMetadataResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.HidePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListPostReportsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LockPostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPostAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PictrsImages
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostReportResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.RemovePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ResolvePostReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SavePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SuccessResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Url
import io.ktor.client.request.forms.MultiPartFormDataContent

interface PostService {
    @GET("post/list")
    suspend fun getAll(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: SortType? = null,
        @Query("page") page: Int? = null,
        @Query("page_cursor") pageCursor: String? = null,
        @Query("type_") type: ListingType? = null,
        @Query("community_id") communityId: CommunityId? = null,
        @Query("community_name") communityName: String? = null,
        @Query("saved_only") savedOnly: Boolean? = null,
        @Query("liked_only") likedOnly: Boolean? = null,
        @Query("disliked_only") dislikedOnly: Boolean? = null,
    ): GetPostsResponse

    @GET("post")
    suspend fun get(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("id") id: PostId? = null,
        @Query("comment_id") commentId: CommentId? = null,
    ): GetPostResponse

    @GET("post/site_metadata")
    suspend fun getSiteMetadata(
        @Header("Authorization") authHeader: String? = null,
        @Query("url")
        url: String,
    ): GetSiteMetadataResponse

    @PUT("post/save")
    @Headers("Content-Type: application/json")
    suspend fun save(
        @Header("Authorization") authHeader: String? = null,
        @Body form: SavePostForm,
    ): PostResponse

    @POST("post/like")
    @Headers("Content-Type: application/json")
    suspend fun like(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostLikeForm,
    ): PostResponse

    @POST("post")
    @Headers("Content-Type: application/json")
    suspend fun create(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostForm,
    ): PostResponse

    @PUT("post")
    @Headers("Content-Type: application/json")
    suspend fun edit(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditPostForm,
    ): PostResponse

    @POST("post/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkPostAsReadForm,
    ): PostResponse

    @POST("post/hide")
    @Headers("Content-Type: application/json")
    suspend fun hide(
        @Header("Authorization") authHeader: String? = null,
        @Body form: HidePostForm,
    ): PostResponse

    @POST("post/delete")
    @Headers("Content-Type: application/json")
    suspend fun delete(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DeletePostForm,
    ): PostResponse

    @POST
    suspend fun uploadImage(
        @Url url: String,
        @Header("Cookie") token: String,
        @Header("Authorization") authHeader: String? = null,
        @Body content: MultiPartFormDataContent,
    ): PictrsImages

    @POST("post/report")
    @Headers("Content-Type: application/json")
    suspend fun createReport(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostReportForm,
    ): PostReportResponse

    @POST("post/feature")
    @Headers("Content-Type: application/json")
    suspend fun feature(
        @Header("Authorization") authHeader: String? = null,
        @Body form: FeaturePostForm,
    ): PostResponse

    @POST("post/remove")
    @Headers("Content-Type: application/json")
    suspend fun remove(
        @Header("Authorization") authHeader: String? = null,
        @Body form: RemovePostForm,
    ): PostResponse

    @POST("post/lock")
    @Headers("Content-Type: application/json")
    suspend fun lock(
        @Header("Authorization") authHeader: String? = null,
        @Body form: LockPostForm,
    ): PostResponse

    @GET("post/report/list")
    @Headers("Content-Type: application/json")
    suspend fun listReports(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("unresolved_only") unresolvedOnly: Boolean? = null,
        @Query("community_id") communityId: CommunityId? = null,
    ): ListPostReportsResponse

    @PUT("post/report/resolve")
    @Headers("Content-Type: application/json")
    suspend fun resolveReport(
        @Header("Authorization") authHeader: String? = null,
        @Body form: ResolvePostReportForm,
    ): PostReportResponse

    @POST("admin/purge/post")
    @Headers("Content-Type: application/json")
    suspend fun purge(
        @Header("Authorization") authHeader: String? = null,
        @Body form: PurgePostForm,
    ): SuccessResponse
}
