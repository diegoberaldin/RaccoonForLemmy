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

internal fun String.sanitize(): String = run {
    replace("&amp;", "&")
}.run {
    replace("&nbsp;", " ")
}.fixBlankLinesForSpoilers()

private fun String.fixBlankLinesForSpoilers(): String = run {
    val finalLines = mutableListOf<String>()
    var finalLinesSizeAtLastSpoiler = 0
    lines().forEach { line ->
        if (line.contains(SpoilerRegex.spoilerOpenRegex)) {
            if (finalLines.lastOrNull()?.isEmpty() == false) {
                finalLines += ""
            }
            finalLines += line
            finalLinesSizeAtLastSpoiler = finalLines.size
        } else if (line.isNotEmpty()) {
            finalLines += line
        } else if (finalLinesSizeAtLastSpoiler != finalLines.size) {
            finalLines += ""
        }
    }
    finalLines.joinToString("\n")
}
