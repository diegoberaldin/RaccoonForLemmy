package com.github.diegoberaldin.raccoonforlemmy.core.utils

import android.os.Build
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier

/*
 * There is a weird bug on Android Q and below due to which the IME padding is added twice and
 * results in a blank space above the virtual keyboard (the same size of the keyboard itself).
 *
 * This workaround simply ignores the imePadding() modifier in those cases.
 */
actual fun Modifier.safeImePadding(): Modifier = then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Modifier.imePadding()
    } else Modifier
)
