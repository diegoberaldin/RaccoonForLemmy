package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

actual fun getTemporaryKeyStore(name: String): TemporaryKeyStore {
    return TemporaryKeyStoreHelper.getTemporaryKeyStore(name)
}
