package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

actual fun getTemporaryKeyStore(name: String): TemporaryKeyStore {
    val res by KoinJavaComponent.inject<TemporaryKeyStore>(
        clazz = TemporaryKeyStore::class.java,
        qualifier = named("custom"),
        parameters = { parametersOf(name) })
    return res
}
