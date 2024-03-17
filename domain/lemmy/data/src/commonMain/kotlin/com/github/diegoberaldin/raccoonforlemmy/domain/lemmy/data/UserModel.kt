package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data

data class UserModel(
    val id: Int = 0,
    val instanceId: Int = 0,
    val name: String = "",
    val displayName: String = "",
    val avatar: String? = null,
    val bio: String? = null,
    val matrixUserId: String? = null,
    val banner: String? = null,
    val host: String = "",
    val score: UserScoreModel? = null,
    val accountAge: String = "",
    val banned: Boolean = false,
    val updateDate: String? = null,
    val admin: Boolean = false,
    val moderator: Boolean = false,
)

fun List<UserModel>.containsId(value: Int?): Boolean = any { it.id == value }

fun UserModel.readableName(preferNickname: Boolean): String {
    return if (preferNickname) {
        displayName.takeIf { it.isNotEmpty() } ?: readableHandle
    } else {
        readableHandle
    }
}

val UserModel.readableHandle: String
    get() = buildString {
        append(name)
        if (host.isNotEmpty()) {
            append("@$host")
        }
    }
