package com.github.diegoberaldin.raccoonforlemmy.core.utils.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class DefaultLruCache<T>(val size: Int) : LruCache<T> {

    private val values: MutableMap<Long, T> = mutableMapOf()
    private var lastUsedIds: List<Long> = listOf()
    private val mutex = Mutex()

    override suspend fun get(key: Long): T? = mutex.withLock {
        val res = values[key]
        if (res != null) {
            moveAtTheBeginning(key)
        }
        return res
    }

    override suspend fun put(value: T, key: Long) = mutex.withLock {
        val old = values[key]
        if (old != null) {
            // already existing element
            values[key] = value
            moveAtTheBeginning(key)
        } else {
            // new element
            values[key] = value
            lastUsedIds = buildList {
                this += key
                this += lastUsedIds
            }
            if (lastUsedIds.size > size) {
                dropOldest()
            }
        }
    }

    private fun dropOldest() {
        lastUsedIds.lastOrNull()?.also { oldest ->
            values.remove(oldest)
            lastUsedIds = lastUsedIds - oldest
        }
    }

    private fun moveAtTheBeginning(key: Long) {
        lastUsedIds = buildList {
            this += key
            this += (lastUsedIds - key)
        }
    }
}
