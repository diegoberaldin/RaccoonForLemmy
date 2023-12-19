package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreatePrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.EditPrivateMessageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.MarkPrivateMessageAsReadForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PrivateMessageResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PrivateMessagesResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query

interface PrivateMessageService {
    @GET("private_message/list")
    suspend fun getPrivateMessages(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("page") page: Int? = null,
        @Query("creator_id") creatorId: PersonId? = null,
        @Query("limit") limit: Int? = null,
        @Query("unread_only") unreadOnly: Boolean? = null,
    ): Response<PrivateMessagesResponse>

    @POST("private_message")
    @Headers("Content-Type: application/json")
    suspend fun createPrivateMessage(
        @Header("Authorization") authHeader: String? = null,
        @Body form: CreatePrivateMessageForm,
    ): Response<PrivateMessageResponse>

    @PUT("private_message")
    @Headers("Content-Type: application/json")
    suspend fun editPrivateMessage(
        @Header("Authorization") authHeader: String? = null,
        @Body form: EditPrivateMessageForm,
    ): Response<PrivateMessageResponse>

    @POST("private_message/mark_as_read")
    @Headers("Content-Type: application/json")
    suspend fun markPrivateMessageAsRead(
        @Header("Authorization") authHeader: String? = null,
        @Body form: MarkPrivateMessageAsReadForm,
    ): Response<PrivateMessageResponse>
}
