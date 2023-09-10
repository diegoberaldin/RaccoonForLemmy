package com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.flow.SharedFlow

interface NavigationCoordinator {

    val onDoubleTabSelection: SharedFlow<Tab>

    fun setCurrentSection(tab: Tab)

    fun setRootNavigator(value: Navigator?)

    fun getRootNavigator(): Navigator?

    fun setBottomBarScrollConnection(value: NestedScrollConnection?)

    fun getBottomBarScrollConnection(): NestedScrollConnection?
}
