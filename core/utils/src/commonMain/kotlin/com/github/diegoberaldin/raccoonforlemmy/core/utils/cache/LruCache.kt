package com.github.diegoberaldin.raccoonforlemmy.core.utils.cache

interface LruCache<T> {
    suspend fun get(key: Int): T?

    suspend fun put(value: T, key: Int)

    companion object {
        fun <T> factory(size: Int): LruCache<T> = DefaultLruCache(size)
    }
}
