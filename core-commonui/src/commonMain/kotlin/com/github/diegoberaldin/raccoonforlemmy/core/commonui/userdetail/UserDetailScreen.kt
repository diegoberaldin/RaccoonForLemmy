package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getUserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments.UserDetailCommentsScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts.UserDetailPostsScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class UserDetailScreen(
    private val user: UserModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel(user.id.toString()) { getUserDetailViewModel(user) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val notificationCenter = remember { getNotificationCenter() }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.xs),
            topBar =
            {
                val communityName = user.name
                val communityHost = user.host
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = buildString {
                                append(communityName)
                                if (communityHost.isNotEmpty()) {
                                    append("@$communityHost")
                                }
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                val sheet = SortBottomSheet(
                                    expandTop = true,
                                )
                                notificationCenter.addObserver({
                                    (it as? SortType)?.also { sortType ->
                                        model.reduce(UserDetailMviModel.Intent.ChangeSort(sortType))
                                    }
                                }, key, NotificationCenterContractKeys.ChangeSortType)
                                bottomSheetNavigator.show(sheet)
                            },
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                navigator?.pop()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val screens = remember(user.id) {
                    val postsScreen = UserDetailPostsScreen(user)
                    val commentsScreen = UserDetailCommentsScreen(user)

                    notificationCenter.addObserver({
                        (it as? UserDetailSection)?.also { section ->
                            model.reduce(UserDetailMviModel.Intent.SelectTab(section))
                        }
                    }, key, NotificationCenterContractKeys.SectionChanged)
                    notificationCenter.addObserver({
                        (it as? UserDetailSection)?.also { section ->
                            model.reduce(UserDetailMviModel.Intent.SelectTab(section))
                        }
                    }, key, NotificationCenterContractKeys.SectionChanged)

                    listOf(
                        postsScreen,
                        commentsScreen,
                    )
                }
                TabNavigator(screens.first()) {
                    CurrentScreen()

                    val tabNavigator = LocalTabNavigator.current
                    LaunchedEffect(model) {
                        model.uiState.map { state -> state.currentTab }.distinctUntilChanged()
                            .onEach { tab ->
                                val index = when (tab) {
                                    UserDetailSection.POSTS -> 0
                                    UserDetailSection.COMMENTS -> 1
                                }
                                tabNavigator.current = screens[index]
                            }.launchIn(this)
                    }
                }
            }
        }
    }
}
