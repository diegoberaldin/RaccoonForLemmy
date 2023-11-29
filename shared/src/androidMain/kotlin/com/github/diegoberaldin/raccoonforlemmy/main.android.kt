package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.runtime.Composable

@Composable
fun MainView(onLoadingFinished: () -> Unit = {}) = App(onLoadingFinished)
