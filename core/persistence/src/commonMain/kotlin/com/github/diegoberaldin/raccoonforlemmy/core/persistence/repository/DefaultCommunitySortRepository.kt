package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

private const val SETTINGS_KEY = "communitySort"

internal class DefaultCommunitySortRepository(
    private val keyStore: TemporaryKeyStore,
) : CommunitySortRepository {

    override fun getSort(handle: String): Int? {
        val map = deserializeMap()
        return map[handle]
    }

    override fun saveSort(handle: String, value: Int) {
        val map = deserializeMap()
        map[handle] = value
        val newValue = serializeMap(map)
        keyStore.save(SETTINGS_KEY, newValue)
    }

    private fun deserializeMap(): MutableMap<String, Int> = keyStore.get(SETTINGS_KEY, listOf()).mapNotNull {
        it.split(":").takeIf { e -> e.size == 2 }?.let { e -> e[0] to e[1] }
    }.let { pairs ->
        val res = mutableMapOf<String, Int>()
        for (pair in pairs) {
            res[pair.first] = pair.second.toInt()
        }
        res
    }

    private fun serializeMap(map: Map<String, Int>): List<String> = map.map { e ->
        e.key + ":" + e.value
    }

    override fun clear() {
        keyStore.remove(SETTINGS_KEY)
    }
}
