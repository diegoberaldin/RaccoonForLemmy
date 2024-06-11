package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.DeleteImageForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MediaModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DefaultMediaRepository(
    private val services: ServiceProvider,
) : MediaRepository {
    override suspend fun getAll(
        auth: String?,
        page: Int,
        limit: Int,
    ): List<MediaModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.user.listMedia(
                        authHeader = auth.toAuthHeader(),
                        page = page,
                        limit = limit,
                    )
                response.images.map { it.toModel() }
            }.getOrElse { emptyList() }
        }

    override suspend fun delete(
        auth: String?,
        media: MediaModel,
    ) = withContext(Dispatchers.IO) {
        val data =
            DeleteImageForm(
                filename = media.alias,
                token = media.deleteToken,
            )
        val res =
            services.user.deleteImage(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
        require(res)
    }
}
