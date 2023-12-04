package com.github.diegoberaldin.raccoonforlemmy.core.navigation.di

import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.navigation.NavigationCoordinator

expect fun getNavigationCoordinator(): NavigationCoordinator

expect fun getDrawerCoordinator(): DrawerCoordinator
