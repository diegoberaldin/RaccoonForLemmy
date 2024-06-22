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

internal fun String.sanitize(): String =
    this
        .removeHtmlEntities()
        .spoilerFixUp()
        .quoteFixUp()
        .expandLemmyHandles()
        .dollarSignFixUp()
        .unescapeMarkdown()
        .emptyLinkFixup()
        .imageBeforeFixup()
        .imageAfterFixup()

private fun String.removeHtmlEntities(): String =
    replace("&amp;", "&")
        .replace("&nbsp;", " ")
        .replace("&hellip;", "…")

private fun String.spoilerFixUp(): String =
    run {
        val finalLines = mutableListOf<String>()
        var isInsideSpoiler = false
        lines().forEach { line ->
            if (line.contains(SpoilerRegex.spoilerOpening)) {
                if (finalLines.lastOrNull()?.isEmpty() == false) {
                    finalLines += ""
                }
                finalLines += line
                isInsideSpoiler = true
            } else if (line.contains(SpoilerRegex.spoilerClosing)) {
                isInsideSpoiler = false
            } else if (line.isNotBlank()) {
                if (isInsideSpoiler) {
                    // spoilers must be treated as a single paragraph, so if inside spoilers it is necessary to remove
                    // all bulleted lists, numbered lists and blank lines in general
                    val cleanLine =
                        line
                            .replace(Regex("^\\s*?-\\s*?"), "")
                            .replace(Regex("^\\s*?\\d?\\.\\s*?"), "")
                            .trim()
                    if (cleanLine.isNotBlank()) {
                        finalLines += cleanLine
                    }
                } else {
                    finalLines += line
                }
            } else if (!isInsideSpoiler) {
                finalLines += ""
            }
        }
        finalLines.joinToString("\n")
    }

private fun String.quoteFixUp(): String =
    run {
        val finalLines = mutableListOf<String>()
        lines().forEach { line ->
            // removes list inside quotes
            val quoteAndList = Regex("^>-")
            val cleanLine = line.replace(quoteAndList, "-")
            val isLastEmpty = finalLines.isNotEmpty() && finalLines.last().isEmpty()
            if (!isLastEmpty || cleanLine.isNotEmpty()) {
                finalLines += cleanLine
            }
        }
        finalLines.joinToString("\n")
    }

private fun String.expandLemmyHandles(): String = LemmyLinkRegex.lemmyHandle.replace(this, "[$1@$2](!$1@$2)")

private fun String.dollarSignFixUp(): String =
    // due to a bug in how the renderer builds annotated strings, replace with full width dollar sign
    replace("$", "\uff04")

private fun String.emptyLinkFixup(): String = replace("[]()", "")

private fun String.imageBeforeFixup(): String =
// due to a bug in the renderer, images after a new line must be on a paragraph on their own,
    // so an additional newline must be inserted (they are nor inline nor a block otherwise)
    ImageRegex.imageNotAfter2Newlines.replace(this, "$1\n\n$2")

private fun String.imageAfterFixup(): String =
    ImageRegex.imageAddNewLineAfter.replace(this, "$1$2\n\n$3")

private fun String.unescapeMarkdown(): String =
    // due to a bug in the library, markdown escapes are NOT recognized, quick workaround to replace them with similar characters
    replace("\\*", "\u2217")
        .replace("\\_", "\uff3f")
        .replace("\\~", "\u2035")
        .replace("\\#", "\u266f")
        .replace("\\>", "\u232a")
