package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

class CommunityInfoScreen(
    private val community: CommunityModel,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getCommunityInfoViewModel(community) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                )
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .width(60.dp)
                    .height(1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(1.dp),
                    ),
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = buildString {
                                        append(uiState.community.name)
                                        if (uiState.community.host.isNotEmpty()) {
                                            append("@${uiState.community.host}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        })
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val themeRepository = remember { getThemeRepository() }
                    val fontScale by themeRepository.contentFontScale.collectAsState()
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density = LocalDensity.current.density,
                            fontScale = fontScale,
                        ),
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = uiState.community.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
