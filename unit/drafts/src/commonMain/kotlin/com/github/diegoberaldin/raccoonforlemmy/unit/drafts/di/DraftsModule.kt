package com.github.diegoberaldin.raccoonforlemmy.unit.drafts.di

import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.DraftsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.DraftsViewModel
import org.koin.dsl.module

val draftsModule =
    module {
        factory<DraftsMviModel> {
            DraftsViewModel(
                themeRepository = get(),
                accountRepository = get(),
                draftRepository = get(),
                notificationCenter = get(),
            )
        }
    }
