package com.github.diegoberaldin.raccoonforlemmy.core.navigation.di

import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import org.koin.java.KoinJavaComponent.inject


actual fun getNavigationCoordinator(): NavigationCoordinator {
    val res: NavigationCoordinator by inject(NavigationCoordinator::class.java)
    return res
}

actual fun getDrawerCoordinator(): DrawerCoordinator {
    val res: DrawerCoordinator by inject(DrawerCoordinator::class.java)
    return res
}
