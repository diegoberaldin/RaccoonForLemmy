package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import org.koin.java.KoinJavaComponent

actual fun getTemporaryKeyStore(): TemporaryKeyStore {
    val res by KoinJavaComponent.inject<TemporaryKeyStore>(TemporaryKeyStore::class.java)
    return res
}
