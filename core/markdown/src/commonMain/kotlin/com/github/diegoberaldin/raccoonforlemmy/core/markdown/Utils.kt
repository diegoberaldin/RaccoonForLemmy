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
}.run {
    // always prepend a newline before spoilers
    replace("::: spoiler ", "\n::: spoiler ")
}