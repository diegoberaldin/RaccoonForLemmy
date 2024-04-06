package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.*
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query

interface CommentService {
    @GET("comment/list")
    suspend fun getAll(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: CommentSortType? = null,
        @Query("post_id") postId: PostId? = null,
        @Query("parent_id") parentId: CommentId? = null,
        @Query("page") page: Int? = null,
        @Query("max_depth") maxDepth: Int? = null,
        @Query("type_") type: ListingType? = null,
        @Query("community_id") communityId: CommunityId? = null,
        @Query("community_name") communityName: String? = null,
        @Query("saved_only") savedOnly: Boolean? = null,
        @Query("liked_only") likedOnly: Boolean? = null,
        @Query("disliked_only") dislikedOnly: Boolean? = null,
    ): Response<GetCommentsResponse>

    @GET("comment")
    suspend fun getBy(
        @Header("Authorization") authHeader: String? = null,
        @Query("id") id: CommentId,
        @Query("auth") auth: String? = null,
    ): Response<GetCommentResponse>

    @PUT("comment/save")
    @Headers("Content-Type: application/json")
    suspend fun save(
        @Header("Authorization") authHeader: String? = null,
        @Body form: SaveCommentForm,
    ): Response<CommentResponse>

    @POST("comment/like")
    @Headers("Content-Type: application/json")
    suspend fun like(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreateCommentLikeForm,
    ): Response<CommentResponse>

    @POST("comment")
    @Headers("Content-Type: application/json")
    suspend fun create(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreateCommentForm,
    ): Response<CommentResponse>

    @PUT("comment")
    @Headers("Content-Type: application/json")
    suspend fun edit(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditCommentForm,
    ): Response<CommentResponse>

    @POST("comment/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkCommentAsReadForm,
    ): Response<CommentReplyResponse>

    @POST("comment/delete")
    @Headers("Content-Type: application/json")
    suspend fun delete(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DeleteCommentForm,
    ): Response<CommentResponse>

    @POST("comment/report")
    @Headers("Content-Type: application/json")
    suspend fun createReport(
        @Body form: CreateCommentReportForm,
        @Header("Authorization") authHeader: String? = null,
    ): Response<CommentReportResponse>

    @POST("comment/remove")
    @Headers("Content-Type: application/json")
    suspend fun remove(
        @Header("Authorization") authHeader: String? = null,
        @Body form: RemoveCommentForm,
    ): Response<CommentResponse>

    @POST("comment/distinguish")
    @Headers("Content-Type: application/json")
    suspend fun distinguish(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DistinguishCommentForm,
    ): Response<CommentResponse>

    @GET("comment/report/list")
    @Headers("Content-Type: application/json")
    suspend fun listReports(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("unresolved_only") unresolvedOnly: Boolean? = null,
        @Query("community_id") communityId: CommunityId? = null,
    ): Response<ListCommentReportsResponse>

    @PUT("comment/report/resolve")
    @Headers("Content-Type: application/json")
    suspend fun resolveReport(
        @Header("Authorization") authHeader: String? = null,
        @Body form: ResolveCommentReportForm,
    ): Response<CommentReportResponse>
}
