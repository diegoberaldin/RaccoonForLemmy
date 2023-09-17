package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.ProfileContentViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.comments.ProfileCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.posts.ProfilePostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved.ProfileSavedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel

expect fun getProfileScreenModel(): ProfileContentViewModel

expect fun getLoginBottomSheetViewModel(): LoginBottomSheetViewModel

expect fun getProfileLoggedViewModel(): ProfileLoggedViewModel

expect fun getProfilePostsViewModel(
    user: UserModel,
): ProfilePostsViewModel

expect fun getProfileCommentsViewModel(user: UserModel): ProfileCommentsViewModel

expect fun getProfileSavedViewModel(user: UserModel): ProfileSavedViewModel
