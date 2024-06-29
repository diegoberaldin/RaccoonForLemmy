package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import kotlinx.coroutines.flow.StateFlow

interface LemmyValueCache {
    val isCurrentUserAdmin: StateFlow<Boolean>
    val isCurrentUserModerator: StateFlow<Boolean>
    val isDownVoteEnabled: StateFlow<Boolean>
    val isCommunityCreationAdminOnly: StateFlow<Boolean>

    suspend fun refresh(auth: String? = null)
}
