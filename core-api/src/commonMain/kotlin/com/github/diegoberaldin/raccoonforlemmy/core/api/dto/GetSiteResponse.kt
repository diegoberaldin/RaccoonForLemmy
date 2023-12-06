package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetSiteResponse(
    @SerialName("site_view") val siteView: SiteView? = null,
    @SerialName("admins") val admins: List<PersonView> = emptyList(),
    @SerialName("version") val version: String? = null,
    @SerialName("my_user") val myUser: MyUserInfo? = null,
    @SerialName("all_languages") val allLanguages: List<Language> = emptyList(),
    @SerialName("discussion_languages") val discussionLanguages: List<LanguageId> = emptyList(),
    @SerialName("taglines") val taglines: List<Tagline> = emptyList(),
    @SerialName("custom_emojis") val customEmojis: List<CustomEmojiView> = emptyList(),
)
