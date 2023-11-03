package com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getManageAccountsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ManageAccountsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getManageAccountsViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    ManageAccountsMviModel.Effect.Close -> {
                        bottomSheetNavigator.hide()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                modifier = Modifier.padding(top = Spacing.s),
                                text = stringResource(MR.strings.manage_accounts_title),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxWidth(),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemsIndexed(uiState.accounts) { idx, account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onClick(
                                    rememberCallback {
                                        model.reduce(ManageAccountsMviModel.Intent.SwitchAccount(idx))
                                    },
                                )
                                .padding(
                                    horizontal = Spacing.m,
                                    vertical = Spacing.s,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val avatar = account.avatar.orEmpty()
                            val iconSize = 32.dp
                            if (avatar.isNotEmpty()) {
                                CustomImage(
                                    modifier = Modifier
                                        .padding(Spacing.xxxs)
                                        .size(iconSize)
                                        .clip(RoundedCornerShape(iconSize / 2)),
                                    url = avatar,
                                    autoload = uiState.autoLoadImages,
                                    quality = FilterQuality.Low,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                )
                            } else {
                                Box(modifier = Modifier.size(iconSize))
                            }
                            Text(
                                text = buildString {
                                    append(account.username)
                                    append("@")
                                    append(account.instance)
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (account.active) {
                                RadioButton(
                                    selected = true,
                                    onClick = null,
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Button(
                            onClick = {
                                bottomSheetNavigator.show(LoginBottomSheet())
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Text(stringResource(MR.strings.manage_accounts_button_add))
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }
                }
            }
        }
    }
}