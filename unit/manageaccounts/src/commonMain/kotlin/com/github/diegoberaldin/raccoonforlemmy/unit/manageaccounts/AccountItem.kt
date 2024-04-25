package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp

@Composable
internal fun AccountItem(
    modifier: Modifier = Modifier,
    account: AccountModel,
    autoLoadImages: Boolean = true,
    onClick: (() -> Unit)? = null,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    var optionsMenuOpen by remember { mutableStateOf(false) }
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onClick(
                onClick = {
                    onClick?.invoke()
                },
            ).padding(
                horizontal = Spacing.m,
                vertical = Spacing.s,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val avatar = account.avatar.orEmpty()
        if (avatar.isNotEmpty()) {
            CustomImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(IconSize.l)
                    .clip(RoundedCornerShape(IconSize.l / 2)),
                url = avatar,
                autoload = autoLoadImages,
                quality = FilterQuality.Low,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Box(modifier = Modifier.size(IconSize.l))
        }
        Text(
            text = buildString {
                append(account.username)
                append("@")
                append(account.instance)
            },
            color = fullColor,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (account.active) {
            RadioButton(
                selected = true,
                onClick = null,
            )
        }

        if (options.isNotEmpty()) {
            Box {
                Icon(
                    modifier = Modifier.size(IconSize.m)
                        .padding(Spacing.xs)
                        .onGloballyPositioned {
                            optionsOffset = it.positionInParent()
                        }
                        .onClick(
                            onClick = rememberCallback {
                                optionsMenuOpen = true
                            },
                        ),
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = ancillaryColor,
                )

                CustomDropDown(
                    expanded = optionsMenuOpen,
                    onDismiss = {
                        optionsMenuOpen = false
                    },
                    offset = DpOffset(
                        x = optionsOffset.x.toLocalDp(),
                        y = optionsOffset.y.toLocalDp(),
                    ),
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(option.text)
                            },
                            onClick = rememberCallback {
                                optionsMenuOpen = false
                                onOptionSelected?.invoke(option.id)
                            },
                        )
                    }
                }
            }
        }
    }
}
