package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoMviModel

expect fun getCommunityInfoViewModel(
    community: CommunityModel,
): CommunityInfoMviModel
