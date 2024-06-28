package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockInstanceForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountBansModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountSettingsModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.LanguageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MetadataModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class DefaultSiteRepository(
    private val services: ServiceProvider,
    private val customServices: ServiceProvider,
) : SiteRepository {
    private var lastInstanceForCachedDownVoteEnabled: String = ""
    private var lastInstanceForCachedCommunityCreationAdminOnly: String = ""
    private var cachedDownVoteEnabled: Boolean = true
    private var cachedCommunityCreationAdminOnly: Boolean = false

    init {
        services.currentInstance
    }

    override suspend fun getCurrentUser(auth: String): UserModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                response.myUser?.let {
                    val user = it.localUserView?.person
                    val counts = it.localUserView?.counts
                    user?.toModel()?.copy(score = counts?.toModel())
                }
            }.getOrNull()
        }

    override suspend fun getSiteVersion(
        auth: String?,
        otherInstance: String?,
    ): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (otherInstance.isNullOrEmpty()) {
                    val response =
                        services.site.get(
                            authHeader = auth.toAuthHeader(),
                        )
                    response.version.takeIf { !it.isNullOrEmpty() }
                } else {
                    customServices.changeInstance(otherInstance)
                    val response =
                        customServices.site.get(
                            authHeader = "",
                        )
                    response.version.takeIf { !it.isNullOrEmpty() }
                }
            }.getOrNull()
        }

    override suspend fun block(
        id: Long,
        blocked: Boolean,
        auth: String?,
    ): Unit =
        withContext(Dispatchers.IO) {
            val data =
                BlockInstanceForm(
                    instanceId = id,
                    block = blocked,
                )
            services.site.block(
                authHeader = auth.toAuthHeader(),
                form = data,
            )
            Unit
        }

    override suspend fun getMetadata(url: String): MetadataModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = services.post.getSiteMetadata(url = url)
                response.metadata.toModel()
            }.getOrNull()
        }

    override suspend fun getLanguages(auth: String?): List<LanguageModel> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                response.allLanguages.map { it.toModel() }
            }.getOrElse { emptyList() }
        }

    override suspend fun isDownVoteEnabled(auth: String?): Boolean =
        withContext(Dispatchers.IO) {
            if (lastInstanceForCachedDownVoteEnabled == services.currentInstance) {
                return@withContext cachedDownVoteEnabled
            }
            runCatching {
                if (auth.isNullOrEmpty()) {
                    return@runCatching true
                }
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                (response.siteView?.localSite?.enableDownvotes == true).also {
                    lastInstanceForCachedDownVoteEnabled = services.currentInstance
                    cachedDownVoteEnabled = it
                }
            }.getOrElse { true }
        }

    override suspend fun isCommunityCreationAdminOnly(auth: String?): Boolean =
        withContext(Dispatchers.IO) {
            if (lastInstanceForCachedCommunityCreationAdminOnly == services.currentInstance) {
                return@withContext cachedCommunityCreationAdminOnly
            }
            runCatching {
                if (auth.isNullOrEmpty()) {
                    return@runCatching true
                }
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                (response.siteView?.localSite?.communityCreationAdminOnly == true).also {
                    lastInstanceForCachedCommunityCreationAdminOnly = services.currentInstance
                    cachedCommunityCreationAdminOnly = it
                }
            }.getOrElse { true }
        }

    override suspend fun getAccountSettings(auth: String): AccountSettingsModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                response.myUser?.localUserView?.toModel()
            }.getOrNull()
        }

    override suspend fun updateAccountSettings(
        auth: String,
        value: AccountSettingsModel,
    ): Unit =
        withContext(Dispatchers.IO) {
            val formData = value.toDto().copy(auth = auth)
            services.user.saveUserSettings(
                authHeader = auth.toAuthHeader(),
                form = formData,
            )
        }

    override suspend fun getBans(auth: String): AccountBansModel? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response =
                    services.site.get(
                        auth = auth,
                        authHeader = auth.toAuthHeader(),
                    )
                response.myUser?.run {
                    AccountBansModel(
                        users = personBlocks.map { it.target.toModel() },
                        communities = communityBlocks.map { it.community.toModel() },
                        instances = instanceBlocks.map { it.instance.toModel() },
                    )
                }
            }.getOrNull()
        }
}
