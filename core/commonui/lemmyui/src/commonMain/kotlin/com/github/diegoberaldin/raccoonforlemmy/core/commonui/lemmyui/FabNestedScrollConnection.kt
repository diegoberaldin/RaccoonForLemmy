package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val THRESHOLD = 1f

@Stable
interface FabNestedScrollConnection : NestedScrollConnection {
    val isFabVisible: StateFlow<Boolean>
}

internal class DefaultFabNestedScrollConnection : FabNestedScrollConnection {
    private val fabVisible = MutableStateFlow(true)
    private val scope = CoroutineScope(SupervisorJob())
    override val isFabVisible: StateFlow<Boolean>
        get() = fabVisible
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = true
            )


    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (available.y < -THRESHOLD) {
            fabVisible.value = false
        }
        if (available.y > THRESHOLD) {
            fabVisible.value = true
        }
        return Offset.Zero
    }
}
