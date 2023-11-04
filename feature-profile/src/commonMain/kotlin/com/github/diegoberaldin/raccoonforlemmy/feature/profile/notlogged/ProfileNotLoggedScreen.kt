package com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

internal object ProfileNotLoggedScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(1u, "")
        }

    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = stringResource(MR.strings.profile_not_logged_message),
            )
            Spacer(modifier = Modifier.height(Spacing.l))
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    navigationCoordinator.getBottomNavigator()?.show(
                        LoginBottomSheet(),
                    )
                },
            ) {
                Text(stringResource(MR.strings.profile_button_login))
            }
        }
    }
}
