package com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ZoomableImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareImageBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.getShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class ZoomableImageScreen(
    private val url: String,
    private val source: String = "",
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val model = getScreenModel<ZoomableImageMviModel>(tag = url, parameters = { parametersOf(url) })
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful
        val errorMessage = LocalXmlStrings.current.messageGenericError
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val shareHelper = remember { getShareHelper() }
        val notificationCenter = remember { getNotificationCenter() }

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    ZoomableImageMviModel.Effect.ShareSuccess -> snackbarHostState.showSnackbar(successMessage)
                    ZoomableImageMviModel.Effect.ShareFailure -> snackbarHostState.showSnackbar(errorMessage)
                }
            }.launchIn(this)
        }
        LaunchedEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
        }
        DisposableEffect(key) {
            onDispose {
                drawerCoordinator.setGesturesEnabled(true)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                onClick = {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    actions = {
                        Icon(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .onClick(
                                    onClick = {
                                        model.reduce(
                                            ZoomableImageMviModel.Intent.SaveToGallery(source)
                                        )
                                    },
                                ),
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                        Icon(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .onClick(
                                    onClick = {
                                        if (shareHelper.supportsShareImage) {
                                            val sheet = ShareImageBottomSheet(url, source)
                                            navigationCoordinator.showBottomSheet(sheet)
                                        } else {
                                            notificationCenter.send(
                                                NotificationCenterEvent.ShareImageModeSelected.ModeUrl(url)
                                            )
                                        }
                                    },
                                ),
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )

                        // content scale option menu
                        Box {
                            val options = buildList {
                                this += ContentScale.Fit
                                this += ContentScale.FillWidth
                                this += ContentScale.FillHeight
                            }
                            var optionsExpanded by remember { mutableStateOf(false) }
                            var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                            Image(
                                modifier = Modifier
                                    .padding(horizontal = Spacing.xs)
                                    .onGloballyPositioned {
                                        optionsOffset = it.positionInParent()
                                    }.onClick(
                                        onClick = {
                                            optionsExpanded = true
                                        },
                                    ),
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                            CustomDropDown(
                                expanded = optionsExpanded,
                                onDismiss = {
                                    optionsExpanded = false
                                },
                                offset = DpOffset(
                                    x = optionsOffset.x.toLocalDp(),
                                    y = optionsOffset.y.toLocalDp(),
                                ),
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            val text = when (option) {
                                                ContentScale.FillHeight -> LocalXmlStrings.current.contentScaleFillHeight
                                                ContentScale.FillWidth -> LocalXmlStrings.current.contentScaleFillWidth
                                                else -> LocalXmlStrings.current.contentScaleFit
                                            }
                                            Text(text)
                                        },
                                        onClick = {
                                            optionsExpanded = false
                                            model.reduce(ZoomableImageMviModel.Intent.ChangeContentScale(option))
                                        },
                                    )
                                }
                            }
                        }

                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
            content =
            { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    ZoomableImage(
                        url = url,
                        autoLoadImages = uiState.autoLoadImages,
                        contentScale = uiState.contentScale,
                    )
                }
            }
        )

        if (uiState.loading) {
            ProgressHud()
        }
    }
}
