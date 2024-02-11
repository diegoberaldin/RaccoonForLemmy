package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface ValidationError {
    data object InvalidField : ValidationError
    data object MissingField : ValidationError
}

@Composable
fun ValidationError.toReadableMessage(): String = when (this) {
    ValidationError.InvalidField -> LocalXmlStrings.current.messageInvalidField
    ValidationError.MissingField -> LocalXmlStrings.current.messageMissingField
}