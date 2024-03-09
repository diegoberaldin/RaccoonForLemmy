package com.github.diegoberaldin.raccoonforlemmy.core.preferences.di

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore

expect fun getTemporaryKeyStore(name: String): TemporaryKeyStore
