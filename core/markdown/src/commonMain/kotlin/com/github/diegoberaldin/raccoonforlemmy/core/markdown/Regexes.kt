package com.github.diegoberaldin.raccoonforlemmy.core.markdown

internal object SpoilerRegex {
    val spoilerOpening = Regex("(:::\\s?spoiler\\s+)(?<title>.*)")
    val spoilerClosing = Regex(":::")
}

internal object LemmyLinkRegex {
    private const val DETAIL_FRAGMENT: String = """[a-zA-Z0-9_]{3,}"""

    private const val INSTANCE_FRAGMENT: String =
        """([a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]\.)+[a-zA-Z]{2,}"""

    val lemmyHandle: Regex =
        Regex("(?<!\\S)!(?<detail>$DETAIL_FRAGMENT)(?:@(?<instance>$INSTANCE_FRAGMENT))?\\b")
}

internal object ImageRegex {
    val image = Regex("!\\[[^]]*]\\((?<url>.*?)\\)")
    val imageNotAfter2Newlines = Regex("(?<before>[^\n])\n(?<image>!\\[[^]]*]\\(.*?\\))")
}
