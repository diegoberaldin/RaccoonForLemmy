package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberPickerDialog(
    title: String = "",
    initialValue: Int = 0,
    onClose: (() -> Unit)? = null,
    onSubmit: ((Int) -> Unit)? = null,
) {
    var currentValue by remember { mutableStateOf(initialValue.toString()) }
    BasicAlertDialog(
        onDismissRequest = {
            onClose?.invoke()
        },
    ) {
        Column(
            modifier =
                Modifier
                    .imePadding()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = currentValue,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        autoCorrect = true,
                    ),
                onValueChange = { value ->
                    currentValue = value
                },
            )

            Spacer(modifier = Modifier.height(Spacing.xs))
            Button(
                onClick = {
                    onSubmit?.invoke(currentValue.toIntOrNull() ?: 0)
                },
            ) {
                Text(text = LocalXmlStrings.current.buttonConfirm)
            }
        }
    }
}
