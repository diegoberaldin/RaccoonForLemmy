package com.github.diegoberaldin.raccoonforlemmy.core.navigation.di

import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getNavigationCoordinator() = CoreNavigationHelper.navigationCoordinator

actual fun getDrawerCoordinator() = CoreNavigationHelper.drawerCoordinator

object CoreNavigationHelper : KoinComponent {
    val navigationCoordinator: NavigationCoordinator by inject()
    val drawerCoordinator: DrawerCoordinator by inject()
}
