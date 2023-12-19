package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged.ProfileLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts.ManageAccountsMviModel

expect fun getProfileScreenModel(): ProfileMainMviModel

expect fun getProfileLoggedViewModel(): ProfileLoggedMviModel

expect fun getManageAccountsViewModel(): ManageAccountsMviModel
