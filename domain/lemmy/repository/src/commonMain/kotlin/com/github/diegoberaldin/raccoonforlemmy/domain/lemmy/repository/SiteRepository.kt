package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountBansModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountSettingsModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface SiteRepository {
    suspend fun getCurrentUser(auth: String): UserModel?

    suspend fun getSiteVersion(auth: String? = null, otherInstance: String? = null): String?

    suspend fun block(id: Int, blocked: Boolean, auth: String? = null): Result<Unit>

    suspend fun getMetadata(url: String): MetadataModel?

    suspend fun getLanguages(auth: String?): List<LanguageModel>

    suspend fun getAccountSettings(auth: String): AccountSettingsModel?

    suspend fun updateAccountSettings(
        auth: String,
        value: AccountSettingsModel,
    ): Result<Unit>

    suspend fun getBans(
        auth: String,
    ): AccountBansModel?
}
