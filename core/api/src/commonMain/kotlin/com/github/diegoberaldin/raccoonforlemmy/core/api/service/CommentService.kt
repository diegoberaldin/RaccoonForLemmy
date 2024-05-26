package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentReplyResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentReportResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentSortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DistinguishCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetCommentResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetCommentsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListCommentReportsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkCommentAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgeCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.RemoveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ResolveCommentReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveCommentForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SuccessResponse
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
    ): GetCommentsResponse

    @GET("comment")
    suspend fun getBy(
        @Header("Authorization") authHeader: String? = null,
        @Query("id") id: CommentId,
        @Query("auth") auth: String? = null,
    ): GetCommentResponse

    @PUT("comment/save")
    @Headers("Content-Type: application/json")
    suspend fun save(
        @Header("Authorization") authHeader: String? = null,
        @Body form: SaveCommentForm,
    ): CommentResponse

    @POST("comment/like")
    @Headers("Content-Type: application/json")
    suspend fun like(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreateCommentLikeForm,
    ): CommentResponse

    @POST("comment")
    @Headers("Content-Type: application/json")
    suspend fun create(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreateCommentForm,
    ): CommentResponse

    @PUT("comment")
    @Headers("Content-Type: application/json")
    suspend fun edit(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditCommentForm,
    ): CommentResponse

    @POST("comment/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkCommentAsReadForm,
    ): CommentReplyResponse

    @POST("comment/delete")
    @Headers("Content-Type: application/json")
    suspend fun delete(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DeleteCommentForm,
    ): CommentResponse

    @POST("comment/report")
    @Headers("Content-Type: application/json")
    suspend fun createReport(
        @Body form: CreateCommentReportForm,
        @Header("Authorization") authHeader: String? = null,
    ): CommentReportResponse

    @POST("comment/remove")
    @Headers("Content-Type: application/json")
    suspend fun remove(
        @Header("Authorization") authHeader: String? = null,
        @Body form: RemoveCommentForm,
    ): CommentResponse

    @POST("comment/distinguish")
    @Headers("Content-Type: application/json")
    suspend fun distinguish(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DistinguishCommentForm,
    ): CommentResponse

    @GET("comment/report/list")
    @Headers("Content-Type: application/json")
    suspend fun listReports(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("unresolved_only") unresolvedOnly: Boolean? = null,
        @Query("community_id") communityId: CommunityId? = null,
    ): ListCommentReportsResponse

    @PUT("comment/report/resolve")
    @Headers("Content-Type: application/json")
    suspend fun resolveReport(
        @Header("Authorization") authHeader: String? = null,
        @Body form: ResolveCommentReportForm,
    ): CommentReportResponse

    @POST("admin/purge/comment")
    @Headers("Content-Type: application/json")
    suspend fun purge(
        @Header("Authorization") authHeader: String? = null,
        @Body form: PurgeCommentForm,
    ): SuccessResponse
}
