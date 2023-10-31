package com.github.diegoberaldin.raccoonforlemmy.core.commonui

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

private const val ACTION_ID_COPY = 0
private const val ACTION_ID_SEARCH = 1

class CustomTextToolbar(
    private val view: View,
    private val onSearch: () -> Unit,
) : TextToolbar {
    private var actionMode: ActionMode? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = view.startActionMode(
                CustomTextActionModeCallback(
                    rect = rect,
                    onCopy = {
                        onCopyRequested?.invoke()
                    },
                    onSearch = {
                        onCopyRequested?.invoke()
                        onSearch()
                    },
                ),
                ActionMode.TYPE_FLOATING,
            )
        } else {
            actionMode?.invalidate()
            actionMode = null
        }
    }
}

internal class CustomTextActionModeCallback(
    private val rect: Rect,
    private val onCopy: () -> Unit,
    private val onSearch: () -> Unit,
) : ActionMode.Callback2() {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.apply {
            val groupId = 0
            add(
                groupId, ACTION_ID_COPY, 0, android.R.string.copy
            ).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            add(
                groupId, ACTION_ID_SEARCH, 1, android.R.string.search_go
            ).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val res = when (item?.itemId) {
            ACTION_ID_COPY -> {
                onCopy()
                true
            }

            ACTION_ID_SEARCH -> {
                onSearch()
                true
            }

            else -> false
        }
        mode?.finish()
        return res
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        // no-op
    }

    override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: android.graphics.Rect?) {
        rect.apply {
            outRect?.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
    }
}