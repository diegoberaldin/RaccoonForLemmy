package com.github.diegoberaldin.raccoonforlemmy.core.persistence

import android.util.Base64
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import java.security.SecureRandom

class DefaultDatabaseKeyProvider(
    private val keyStore: TemporaryKeyStore,
) : DatabaseKeyProvider {

    companion object {
        private const val DATABASE_KEY = "database_key"
    }

    override fun getKey(): ByteArray {
        val savedKey = retrieveStoreKey()
        return if (savedKey.isEmpty()) {
            val key = generateKey()
            val keyString = encodeToString(key)
            storeKey(keyString)
            key
        } else {
            val res = decodeFromString(savedKey)
            if (res.isNotEmpty()) {
                res
            } else {
                // regenerates the key
                val key = generateKey()
                val keyString = encodeToString(key)
                storeKey(keyString)
                key
            }
        }
    }

    override fun removeKey() {
        keyStore.remove(DATABASE_KEY)
    }

    private fun retrieveStoreKey(): String = keyStore[DATABASE_KEY, ""]

    private fun storeKey(key: String) = keyStore.save(DATABASE_KEY, key)

    private fun generateKey(): ByteArray {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        return key
    }

    private fun encodeToString(key: ByteArray): String =
        Base64.encodeToString(key, Base64.DEFAULT)

    private fun decodeFromString(key: String): ByteArray {
        return Base64.decode(key, Base64.DEFAULT)
    }
}