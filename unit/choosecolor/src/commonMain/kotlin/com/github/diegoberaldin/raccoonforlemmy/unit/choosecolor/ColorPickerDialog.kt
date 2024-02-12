package com.github.diegoberaldin.raccoonforlemmy.unit.choosecolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toHexDigit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    initialValue: Color = Color.Black,
    onClose: (() -> Unit)? = null,
    onSubmit: ((Color) -> Unit)? = null,
    onReset: (() -> Unit)? = null,
) {
    var currentColor by remember { mutableStateOf(initialValue) }
    BasicAlertDialog(
        onDismissRequest = {
            onClose?.invoke()
        },
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = LocalXmlStrings.current.settingsColorDialogTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .background(
                        color = currentColor,
                        shape = RoundedCornerShape(CornerSize.m),
                    )
                    .fillMaxWidth()
                    .height(100.dp)
            )
            val alpha = (currentColor.alpha * 255).toInt().toHexDigit()
            val red = (currentColor.red * 255).toInt().toHexDigit()
            val green = (currentColor.green * 255).toInt().toHexDigit()
            val blue = (currentColor.blue * 255).toInt().toHexDigit()
            Text(
                text = "#$alpha$red$green$blue",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(Spacing.s))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = LocalXmlStrings.current.settingsColorDialogAlpha,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Slider(
                    value = currentColor.alpha,
                    onValueChange = {
                        currentColor = currentColor.copy(alpha = it)
                    },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = LocalXmlStrings.current.settingsColorDialogRed,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Slider(
                    value = currentColor.red,
                    onValueChange = {
                        currentColor = currentColor.copy(red = it)
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = LocalXmlStrings.current.settingsColorDialogGreen,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Slider(
                    value = currentColor.green,
                    onValueChange = {
                        currentColor = currentColor.copy(green = it)
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = LocalXmlStrings.current.settingsColorDialogBlue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Slider(
                    value = currentColor.blue,
                    onValueChange = {
                        currentColor = currentColor.copy(blue = it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (onReset != null) {
                    Button(
                        onClick = {
                            onReset.invoke()
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonReset)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (initialValue != currentColor) {
                            onSubmit?.invoke(currentColor)
                        }
                    },
                ) {
                    Text(text = LocalXmlStrings.current.buttonConfirm)
                }
                if (onReset == null) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}