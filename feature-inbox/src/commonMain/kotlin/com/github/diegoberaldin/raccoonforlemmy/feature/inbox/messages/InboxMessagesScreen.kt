package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

class InboxMessagesScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(2u, "")
        }

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "\uD83D\uDEA7 Work in progress! \uD83D\uDEA7",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}