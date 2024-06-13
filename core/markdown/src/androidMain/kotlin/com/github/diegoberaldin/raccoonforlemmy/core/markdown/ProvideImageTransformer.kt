package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.model.ImageTransformer

actual fun provideImageTransformer(): ImageTransformer = Coil2ImageTransformerImpl
