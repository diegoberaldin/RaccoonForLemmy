package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode

internal fun ASTNode.findChildOfTypeRecursive(type: IElementType): ASTNode? {
    children.forEach {
        if (it.type == type) {
            return it
        } else {
            val found = it.findChildOfTypeRecursive(type)
            if (found != null) {
                return found
            }
        }
    }
    return null
}

internal fun String.sanitize(): String = this
    .removeEntities()
    .spoilerFixup()
    .quoteFixup()
    .expandLemmyHandles()

private fun String.removeEntities(): String =
    replace("&amp;", "&")
        .replace("&nbsp;", " ")


private fun String.spoilerFixup(): String = run {
    val finalLines = mutableListOf<String>()
    var finalLinesSizeAtLastSpoiler = 0
    lines().forEach { line ->
        val isSpoilerOnTopOfStack = finalLinesSizeAtLastSpoiler == finalLines.size
        if (line.contains(SpoilerRegex.spoilerOpenRegex)) {
            if (finalLines.lastOrNull()?.isEmpty() == false) {
                finalLines += ""
            }
            finalLines += line
            finalLinesSizeAtLastSpoiler = finalLines.size
        } else if (line.isNotBlank()) {
            if (isSpoilerOnTopOfStack) {
                // removes list inside spoilers
                val cleanLine = line.replace(Regex("^\\s*?- "), "").trim()
                if (cleanLine.isNotEmpty()) {
                    finalLines += cleanLine
                }
            } else {
                finalLines += line
            }
        } else if (!isSpoilerOnTopOfStack) {
            finalLines += ""
        }
    }
    finalLines.joinToString("\n")
}

private fun String.quoteFixup(): String = run {
    val finalLines = mutableListOf<String>()
    lines().forEach { line ->
        // removes list inside quotes
        val quoteAndList = Regex("^>\\s*?-")
        if (quoteAndList.matches(line)) {
            val cleanLine = line.replace(quoteAndList, ">")
            if (cleanLine.isNotEmpty()) {
                finalLines += cleanLine
            }
        } else {
            finalLines += line
        }
    }
    finalLines.joinToString("\n")
}

private fun String.expandLemmyHandles(): String = let { content ->
    buildString {
        val matches = LemmyLinkRegex.lemmyHandle.findAll(content)
        var lastIndex = 0
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last
            if (start > lastIndex) {
                append(content.substring(startIndex = lastIndex, endIndex = start))
            }
            val detail = match.groups["detail"]?.value.orEmpty()
            val instance = match.groups["instance"]?.value.orEmpty()
            append("[$detail@$instance](!$detail@$instance)")
            lastIndex = end + 1
        }
        if (lastIndex < content.lastIndex) {
            append(content.substring(startIndex = lastIndex))
        }
    }
}
