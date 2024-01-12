package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.utils

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

private data class Node(
    val comment: CommentModel?,
    val children: MutableList<Node> = mutableListOf(),
)

private fun findNode(id: String, node: Node): Node? {
    if (node.comment?.id.toString() == id) {
        return node
    }
    for (c in node.children) {
        val res = findNode(id, c)
        if (res != null) {
            return res
        }
    }
    return null
}


private fun linearize(node: Node, list: MutableList<CommentModel>) {
    if (node.comment != null) {
        list.add(node.comment)
    }
    for (c in node.children) {
        linearize(c, list)
    }
}

internal fun List<CommentModel>.populateLoadMoreComments() = mapIndexed { idx, comment ->
    val hasMoreComments = (comment.comments ?: 0) > 0
    val isNextCommentNotChild =
        idx < lastIndex && this[idx + 1].depth <= comment.depth
    comment.copy(loadMoreButtonVisible = hasMoreComments && isNextCommentNotChild)
}

internal fun List<CommentModel>.processCommentsToGetNestedOrder(
    ancestorId: String? = null,
): List<CommentModel> {
    val root = Node(null)
    // reconstructs the tree
    val sortedByPath = this.sortedBy { it.path }
    for (c in sortedByPath) {
        val parentId = c.parentId
        if (parentId == ancestorId) {
            root.children += Node(c)
        } else if (parentId != null) {
            val parent = findNode(parentId, root)
            if (parent != null) {
                parent.children += Node(c)
            }
        }
    }

    // linearize the tree depth first
    val result = mutableListOf<CommentModel>()
    linearize(root, result)

    return result.toList()
}
