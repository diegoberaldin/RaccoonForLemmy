package com.github.diegoberaldin.raccoonforlemmy.core.markdown.di

import com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider.DefaultMarkwonProvider
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider.MarkwonProvider
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

val markwonModule = module {
    single<MarkwonProvider> { params ->
        DefaultMarkwonProvider(
            context = get(),
        )
    }
}

internal fun getMarkwonProvider(): MarkwonProvider {
    val res: MarkwonProvider by inject(MarkwonProvider::class.java)
    return res
}
