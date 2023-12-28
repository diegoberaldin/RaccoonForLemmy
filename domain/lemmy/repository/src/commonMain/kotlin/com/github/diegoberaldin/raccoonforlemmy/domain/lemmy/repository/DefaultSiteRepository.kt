package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockSiteForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountBansModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountSettingsModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

internal class DefaultSiteRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : SiteRepository {
    override suspend fun getCurrentUser(auth: String): UserModel? = runCatching {
        val dto = services.site.get(
            auth = auth,
            authHeader = auth.toAuthHeader(),
        ).body()
        dto?.myUser?.let {
            val user = it.localUserView?.person
            val counts = it.localUserView?.counts
            user?.toModel()?.copy(score = counts?.toModel())
        }
    }.getOrNull()

    override suspend fun getSiteVersion(auth: String?, otherInstance: String?): String? =
        runCatching {
            if (otherInstance.isNullOrEmpty()) {
                val dto = services.site.get(
                    authHeader = auth.toAuthHeader(),
                ).body()
                dto?.version.takeIf { !it.isNullOrEmpty() }
            } else {
                customServices.changeInstance(otherInstance)
                val dto = customServices.site.get(
                    authHeader = "",
                ).body()
                dto?.version.takeIf { !it.isNullOrEmpty() }
            }
        }.getOrNull()

    override suspend fun block(id: Int, blocked: Boolean, auth: String?): Result<Unit> =
        runCatching {
            val data = BlockSiteForm(
                instanceId = id,
                block = blocked,
            )
            services.site.block(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
        }

    override suspend fun getMetadata(url: String): MetadataModel? = runCatching {
        val response = services.post.getSiteMetadata(
            url = url,
        )
        response.body()?.metadata?.toModel()
    }.getOrNull()

    override suspend fun getLanguages(auth: String?): List<LanguageModel> = runCatching {
        val response = services.site.get(auth = auth)
        val dto = response.body()
        dto?.allLanguages?.map { it.toModel() }.orEmpty()
    }.getOrElse { emptyList() }

    override suspend fun getAccountSettings(auth: String): AccountSettingsModel? = runCatching {
        val dto = services.site.get(
            auth = auth,
            authHeader = auth.toAuthHeader(),
        ).body()
        dto?.myUser?.localUserView?.run {
            localUser?.toModel()?.copy(
                avatar = person.avatar,
                banner = person.banner,
                bio = person.bio,
                bot = person.botAccount,
                displayName = person.displayName,
                matrixUserId = person.matrixUserId,
            )
        }
    }.getOrNull()

    override suspend fun updateAccountSettings(
        auth: String,
        value: AccountSettingsModel,
    ): Result<Unit> = runCatching {
        val formData = value.toDto().copy(auth = auth)
        services.user.saveUserSettings(
            authHeader = auth.toAuthHeader(),
            form = formData,
        )
        Unit
    }

    override suspend fun getBans(auth: String): AccountBansModel? = runCatching {
        val dto = services.site.get(
            auth = auth,
            authHeader = auth.toAuthHeader(),
        ).body()
        dto?.myUser?.run {
            AccountBansModel(
                users = personBlocks.map { it.target.toModel() },
                communities = communityBlocks.map { it.community.toModel() },
                instances = instanceBlocks.map { it.instance.toModel() },
            )
        }
    }.getOrNull()
}