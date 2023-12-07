package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockSiteForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

internal class DefaultSiteRepository(
    private val services: ServiceProvider,
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

    override suspend fun getSiteVersion(auth: String): String? = runCatching {
        val dto = services.site.get(
            authHeader = auth.toAuthHeader(),
        ).body()
        dto?.version.takeIf { !it.isNullOrEmpty() }
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
        val response = services.site.getSiteMetadata(
            url = url,
        )
        response.body()?.metadata?.toModel()
    }.getOrNull()
}