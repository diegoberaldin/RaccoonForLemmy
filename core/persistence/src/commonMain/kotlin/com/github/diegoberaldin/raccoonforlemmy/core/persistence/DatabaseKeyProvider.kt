package com.github.diegoberaldin.raccoonforlemmy.core.persistence

interface DatabaseKeyProvider {
    fun getKey(): ByteArray

    fun removeKey()
}
