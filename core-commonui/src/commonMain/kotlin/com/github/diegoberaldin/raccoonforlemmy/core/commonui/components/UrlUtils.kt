package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.ui.platform.UriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

fun getCommunityFromUrl(url: String?): CommunityModel? {
    val (normalizedUrl, instance) = normalizeUrl(url)
    val res = extractCommunity(normalizedUrl)
    return if (res != null && res.host.isEmpty()) {
        res.copy(host = instance)
    } else {
        res
    }
}

fun getUserFromUrl(url: String?): UserModel? {
    val (normalizedUrl, instance) = normalizeUrl(url)
    val res = extractUser(normalizedUrl)
    return if (res != null && res.host.isEmpty()) {
        res.copy(host = instance)
    } else {
        res
    }
}

fun getPostFromUrl(url: String?): Pair<PostModel, String>? {
    val (normalizedUrl, instance) = normalizeUrl(url)
    val post = extractPost(normalizedUrl)
    return if (post != null && instance.isNotEmpty()) {
        post to instance
    } else {
        null
    }
}

fun NavigationCoordinator.handleUrl(
    url: String,
    openExternal: Boolean,
    uriHandler: UriHandler,
) {
    val community = getCommunityFromUrl(url)
    val user = getUserFromUrl(url)
    val (post, postInstance) = getPostFromUrl(url) ?: (null to null)

    when {
        community != null -> {
            pushScreen(
                CommunityDetailScreen(
                    community = community,
                    otherInstance = community.host
                )
            )
        }

        user != null -> {
            pushScreen(
                UserDetailScreen(
                    user = user,
                    otherInstance = user.host
                )
            )
        }

        post != null -> {
            pushScreen(
                PostDetailScreen(
                    post = post,
                    otherInstance = postInstance.orEmpty(),
                )
            )
        }

        openExternal -> {
            uriHandler.openUri(url)
        }

        else -> pushScreen(WebViewScreen(url))
    }
}

private fun normalizeUrl(url: String?): Pair<String, String> {
    val matches = Regex("https?://(?<instance>.*?)(?<pathAndQuery>/.*)").findAll(url.orEmpty())
    var instance = ""
    val res = buildString {
        if (matches.count() > 0) {
            val match = matches.iterator().next()
            val value = match.groups["pathAndQuery"]?.value.orEmpty()
            instance = match.groups["instance"]?.value.orEmpty()
            if (value.isNotEmpty()) {
                append(value)
            } else {
                append(url)
            }
        } else {
            append(url)
        }
    }
    return res to instance
}

private fun extractCommunity(url: String): CommunityModel? = when {
    url.startsWith("/c/") -> {
        val cleanString = url.substring(3)
        if (url.count { it == '@' } == 1) {
            val (name, host) = cleanString.split("@", limit = 2).let { l -> l[0] to l[1] }
            CommunityModel(
                name = name,
                host = host,
            )
        } else {
            CommunityModel(
                name = cleanString,
            )
        }
    }

    url.startsWith("!") -> {
        val cleanString = url.substring(1)
        if (url.count { it == '@' } == 1) {
            val (name, host) = cleanString.split("@", limit = 2).let { l -> l[0] to l[1] }
            CommunityModel(
                name = name,
                host = host,
            )
        } else {
            CommunityModel(
                name = cleanString,
            )
        }
    }

    else -> null
}

private fun extractUser(url: String): UserModel? = when {
    url.startsWith("@") -> {
        val cleanString = url.substring(1)
        if (url.count { it == '@' } == 1) {
            val (name, host) = cleanString.split("@", limit = 2).let { l -> l[0] to l[1] }
            UserModel(
                name = name,
                host = host,
            )
        } else {
            UserModel(
                name = cleanString,
            )
        }
    }

    url.startsWith("/u/") -> {
        val cleanString = url.substring(3)
        if (url.count { it == '@' } == 1) {
            val (name, host) = cleanString.split("@", limit = 2).let { l -> l[0] to l[1] }
            UserModel(
                name = name,
                host = host,
            )
        } else {
            UserModel(
                name = cleanString,
            )
        }
    }

    else -> null
}

private fun extractPost(url: String): PostModel? {
    val regex = Regex("/post/(?<postId>.*$)")
    val match = regex.find(url)
    val id = match
        ?.let { it.groups["postId"]?.value.orEmpty() }
        ?.let { runCatching { it.toInt() }.getOrNull() }
    return if (id != null) {
        PostModel(id = id)
    } else {
        null
    }
}