package com.github.diegoberaldin.raccoonforlemmy.core.api.service

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommunityId
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetModlogResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ModlogActionType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PersonId
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query

interface ModlogService {
    @GET("modlog")
    suspend fun getItems(
        @Header("Authorization") authHeader: String? = null,
        @Query("auth") auth: String? = null,
        @Query("page") page: Int? = null,
        @Query("community_id") communityId: CommunityId? = null,
        @Query("limit") limit: Int? = null,
        @Query("mod_person_id") modId: PersonId? = null,
        @Query("other_person_id") otherId: PersonId? = null,
        @Query("type_") type: ModlogActionType? = null,
    ): GetModlogResponse
}
