package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePostReportForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeletePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditPostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPostAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PictrsImages
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostReportResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SavePostForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType
import de.jensklingenberg.ktorfit.Response
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
        @Query("community_id") communityId: Int? = null,
        @Query("community_name") communityName: String? = null,
        @Query("saved_only") savedOnly: Boolean? = null,
    ): Response<GetPostsResponse>

    @GET("post")
    suspend fun get(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("id") id: Int? = null,
        @Query("comment_id") commentId: Int? = null,
    ): Response<GetPostResponse>

    @PUT("post/save")
    @Headers("Content-Type: application/json")
    suspend fun save(
        @Header("Authorization") authHeader: String? = null,
        @Body form: SavePostForm,
    ): Response<PostResponse>

    @POST("post/like")
    @Headers("Content-Type: application/json")
    suspend fun like(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostLikeForm,
    ): Response<PostResponse>

    @POST("post")
    @Headers("Content-Type: application/json")
    suspend fun create(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostForm,
    ): Response<PostResponse>

    @PUT("post")
    @Headers("Content-Type: application/json")
    suspend fun edit(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditPostForm,
    ): Response<PostResponse>

    @POST("post/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkPostAsReadForm,
    ): Response<PostResponse>

    @POST("post/delete")
    @Headers("Content-Type: application/json")
    suspend fun delete(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DeletePostForm,
    ): Response<PostResponse>

    @POST
    suspend fun uploadImage(
        @Url url: String,
        @Header("Cookie") token: String,
        @Header("Authorization") authHeader: String? = null,
        @Body content: MultiPartFormDataContent,
    ): Response<PictrsImages>

    @POST("post/report")
    @Headers("Content-Type: application/json")
    suspend fun createReport(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePostReportForm,
    ): Response<PostReportResponse>
}
