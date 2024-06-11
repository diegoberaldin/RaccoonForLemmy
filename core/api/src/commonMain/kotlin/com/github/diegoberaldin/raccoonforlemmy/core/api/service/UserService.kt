package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockPersonForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockPersonResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentSortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteImageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPersonDetailsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPersonMentionsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetRepliesResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListMediaResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkAllAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPersonMentionAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonMentionResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PurgePersonForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveUserSettingsForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SaveUserSettingsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SuccessResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query

interface UserService {
    @GET("user")
    suspend fun getDetails(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("community_id") communityId: CommunityId? = null,
        @Query("person_id") personId: PersonId? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: CommentSortType = CommentSortType.New,
        @Query("username") username: String? = null,
        @Query("saved_only") savedOnly: Boolean? = null,
    ): GetPersonDetailsResponse

    @GET("user/mention")
    suspend fun getMentions(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: CommentSortType = CommentSortType.New,
        @Query("unread_only") unreadOnly: Boolean? = null,
    ): GetPersonMentionsResponse

    @GET("user/replies")
    suspend fun getReplies(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: CommentSortType = CommentSortType.New,
        @Query("unread_only") unreadOnly: Boolean? = null,
    ): GetRepliesResponse

    @POST("user/mark_all_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markAllAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkAllAsReadForm,
    ): GetRepliesResponse

    @POST("user/mention/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markPersonMentionAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkPersonMentionAsReadForm,
    ): PersonMentionResponse

    @POST("user/block")
    @Headers("Content-Type: application/json")
    suspend fun block(
        @Header("Authorization") authHeader: String? = null,
        @Body form: BlockPersonForm,
    ): BlockPersonResponse

    @PUT("user/save_user_settings")
    @Headers("Content-Type: application/json")
    suspend fun saveUserSettings(
        @Header("Authorization") authHeader: String? = null,
        @Body form: SaveUserSettingsForm,
    ): SaveUserSettingsResponse

    @POST("admin/purge/person")
    @Headers("Content-Type: application/json")
    suspend fun purge(
        @Header("Authorization") authHeader: String? = null,
        @Body form: PurgePersonForm,
    ): SuccessResponse

    @GET("account/list_media")
    suspend fun listMedia(
        @Header("Authorization") authHeader: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): ListMediaResponse

    @POST("image/delete")
    @Headers("Content-Type: application/json")
    suspend fun deleteImage(
        @Header("Authorization") authHeader: String? = null,
        @Body form: DeleteImageForm,
    ): Boolean
}
