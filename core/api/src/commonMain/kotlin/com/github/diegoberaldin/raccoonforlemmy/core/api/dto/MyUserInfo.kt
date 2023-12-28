package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyUserInfo(
    @SerialName("local_user_view") val localUserView: LocalUserView? = null,
    @SerialName("follows") val follows: List<CommunityFollowerView> = emptyList(),
    @SerialName("moderates") val moderates: List<CommunityModeratorView> = emptyList(),
    @SerialName("community_blocks") val communityBlocks: List<CommunityBlockView> = emptyList(),
    @SerialName("person_blocks") val personBlocks: List<PersonBlockView> = emptyList(),
    @SerialName("instance_blocks") val instanceBlocks: List<InstanceBlockView> = emptyList(),
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId> = emptyList(),
)
