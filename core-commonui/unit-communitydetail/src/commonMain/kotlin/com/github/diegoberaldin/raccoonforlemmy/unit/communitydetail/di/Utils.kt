package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.CommunityDetailMviModel

expect fun getCommunityDetailViewModel(
    community: CommunityModel,
    otherInstance: String = "",
): CommunityDetailMviModel
