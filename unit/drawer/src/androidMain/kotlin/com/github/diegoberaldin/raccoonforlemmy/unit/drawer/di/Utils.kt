package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di

import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.cache.SubscriptionsCache
import org.koin.java.KoinJavaComponent

actual fun getSubscriptionsCache(): SubscriptionsCache {
    val res by KoinJavaComponent.inject<SubscriptionsCache>(SubscriptionsCache::class.java)
    return res
}
