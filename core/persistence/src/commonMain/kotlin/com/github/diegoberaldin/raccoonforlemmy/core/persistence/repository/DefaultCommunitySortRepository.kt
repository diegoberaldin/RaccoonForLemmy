package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

internal class DefaultCommunitySortRepository(
    private val keyStore: TemporaryKeyStore
) : CommunitySortRepository {

    override fun getSort(handle: String): Int? = keyStore[handle, -1].takeIf { it > 0 }

    override fun saveSort(handle: String, value: Int) {
        keyStore.save(handle, value)
    }

    override fun clear() {
        keyStore.removeAll()
    }
}
