package com.github.diegoberaldin.raccoonforlemmy.core.utils.cache

interface LruCache<T> {
    suspend fun get(key: Long): T?

    suspend fun put(
        value: T,
        key: Long,
    )

    companion object {
        fun <T> factory(size: Int): LruCache<T> = DefaultLruCache(size)
    }
}
