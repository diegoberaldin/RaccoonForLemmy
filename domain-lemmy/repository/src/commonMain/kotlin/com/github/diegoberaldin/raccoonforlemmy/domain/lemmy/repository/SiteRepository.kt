package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface SiteRepository {
    suspend fun getCurrentUser(auth: String): UserModel?

    suspend fun getSiteVersion(auth: String): String?

    suspend fun block(id: Int, blocked: Boolean, auth: String? = null): Result<Unit>

    suspend fun getMetadata(url: String): MetadataModel?
}
