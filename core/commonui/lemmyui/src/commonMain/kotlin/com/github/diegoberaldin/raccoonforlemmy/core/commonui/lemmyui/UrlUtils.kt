package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.ui.platform.UriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAVideo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAnImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.CustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.UrlOpeningMode
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
    openingMode: UrlOpeningMode,
    uriHandler: UriHandler,
    customTabsHelper: CustomTabsHelper,
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenUser: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
) {
    val community = getCommunityFromUrl(url)
    val user = getUserFromUrl(url)
    val (post, postInstance) = getPostFromUrl(url) ?: (null to null)
    val isMedia = url.looksLikeAVideo || url.looksLikeAnImage

    when {
        community != null && !isMedia && onOpenCommunity != null -> {
            onOpenCommunity.invoke(community, community.host)
        }

        user != null && !isMedia && onOpenUser != null -> {
            onOpenUser.invoke(user, user.host)
        }

        post != null && !isMedia && onOpenPost != null -> {
            onOpenPost.invoke(post, postInstance.orEmpty())
        }

        openingMode == UrlOpeningMode.External -> {
            runCatching {
                uriHandler.openUri(url)
            }
        }

        openingMode == UrlOpeningMode.CustomTabs -> {
            runCatching {
                customTabsHelper.handle(url)
            }.also {
                it.exceptionOrNull()?.also { e ->
                    e.printStackTrace()
                }
            }
        }

        else -> {
            onOpenWeb?.invoke(url)
        }
    }
}

private fun normalizeUrl(url: String?): Pair<String, String> {
    val matches = Regex("https?://(?<instance>.*?)(?<pathAndQuery>/.*)").findAll(url.orEmpty())
    var instance = ""
    val res =
        buildString {
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

private fun extractCommunity(url: String): CommunityModel? =
    when {
        url.startsWith("/c/") -> {
            val cleanString = url.substring(3)
            if (cleanString.count { it == '@' } == 1) {
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
            if (cleanString.count { it == '@' } == 1) {
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

private fun extractUser(url: String): UserModel? =
    when {
        url.startsWith("@") -> {
            val cleanString = url.substring(1)
            if (cleanString.count { it == '@' } == 1) {
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
            if (cleanString.count { it == '@' } == 1) {
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
    val id =
        match
            ?.let { it.groups["postId"]?.value.orEmpty() }?.toLongOrNull()
    return if (id != null) {
        PostModel(id = id)
    } else {
        null
    }
}
