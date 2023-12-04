package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockSiteForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SiteMetadata
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel

class SiteRepository(
    private val services: ServiceProvider,
) {
    suspend fun getCurrentUser(auth: String): UserModel? = runCatching {
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

    suspend fun block(id: Int, blocked: Boolean, auth: String? = null): Result<Unit> = runCatching {
        val data = BlockSiteForm(
            instanceId = id,
            block = blocked,
        )
        services.site.block(
            authHeader = auth.toAuthHeader(),
            form = data,
        )
    }

    suspend fun getMetadata(url: String): MetadataModel? = runCatching {
        val response = services.site.getSiteMetadata(
            url = url,
        )
        response.body()?.metadata?.toModel()
    }.getOrNull()
}

private fun SiteMetadata.toModel() = MetadataModel(
    title = title.orEmpty(),
    description = description.orEmpty(),
)
