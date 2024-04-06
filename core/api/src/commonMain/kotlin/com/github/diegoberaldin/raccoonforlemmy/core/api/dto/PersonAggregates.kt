package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonAggregates(
    @SerialName("id") val id: Long? = null,
    @SerialName("person_id") val personId: PersonId,
    @SerialName("post_count") val postCount: Int,
    @SerialName("post_score") val postScore: Int? = null,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("comment_score") val commentScore: Int? = null,
)
