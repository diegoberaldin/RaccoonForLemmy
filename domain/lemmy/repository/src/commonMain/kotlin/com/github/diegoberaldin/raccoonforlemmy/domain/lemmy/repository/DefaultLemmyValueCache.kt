package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// TODO: add tests
internal class DefaultLemmyValueCache(
    private val services: ServiceProvider,
) : LemmyValueCache {
    override var isCurrentUserAdmin = MutableStateFlow(false)
    override var isCurrentUserModerator = MutableStateFlow(false)
    override var isDownVoteEnabled = MutableStateFlow(false)
    override var isCommunityCreationAdminOnly = MutableStateFlow(false)

    override suspend fun refresh(auth: String?) {
        val response =
            services.site.get(
                auth = auth,
                authHeader = auth.toAuthHeader(),
            )
        isDownVoteEnabled.update {
            response.siteView?.localSite?.enableDownvotes == true
        }
        isCommunityCreationAdminOnly.update {
            response.siteView?.localSite?.communityCreationAdminOnly == true
        }
        val currentUserId =
            response.myUser
                ?.localUserView
                ?.person
                ?.id
        isCurrentUserAdmin.update {
            response.admins.any { it.person.id == currentUserId }
        }
        isCurrentUserModerator.update {
            response.myUser?.moderates?.isNotEmpty() == true
        }
    }
}
