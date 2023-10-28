package com.github.diegoberaldin.raccoonforlemmy.core.markdown.di

import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.DefaultMarkwonProvider
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.MarkwonProvider
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent

val markwonModule = module {
    factory<MarkwonProvider> { params ->
        DefaultMarkwonProvider(
            context = get(),
            onOpenUrl = params[0],
            onOpenImage = params[1],
        )
    }
}

internal fun getMarkwonProvider(
    onOpenUrl: ((String) -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
): MarkwonProvider {
    val res: MarkwonProvider by KoinJavaComponent.inject(
        MarkwonProvider::class.java,
        parameters = {
            parametersOf(onOpenUrl, onOpenImage)
        },
    )
    return res
}
