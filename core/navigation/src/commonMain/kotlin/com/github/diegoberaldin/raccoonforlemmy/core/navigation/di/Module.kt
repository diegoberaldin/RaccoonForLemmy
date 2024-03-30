package com.github.diegoberaldin.raccoonforlemmy.core.navigation.di

import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DefaultDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DefaultNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.NavigationCoordinator
import org.koin.dsl.module

val navigationModule = module {
    single<NavigationCoordinator> {
        DefaultNavigationCoordinator()
    }
    single<DrawerCoordinator> {
        DefaultDrawerCoordinator()
    }
}
