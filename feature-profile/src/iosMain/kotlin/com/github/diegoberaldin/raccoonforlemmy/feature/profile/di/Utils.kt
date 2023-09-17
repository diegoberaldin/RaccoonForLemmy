package com.github.diegoberaldin.raccoonforlemmy.feature.profile.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.ProfileContentViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.comments.ProfileCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.posts.ProfilePostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved.ProfileSavedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheetViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getProfileScreenModel() = ProfileScreenModelHelper.profileModel

actual fun getLoginBottomSheetViewModel() = ProfileScreenModelHelper.loginModel

actual fun getProfileLoggedViewModel(): ProfileLoggedViewModel =
    ProfileScreenModelHelper.loggedModel

actual fun getProfilePostsViewModel(
    user: UserModel,
): ProfilePostsViewModel =
    ProfileScreenModelHelper.getPostsModel(user = user)

actual fun getProfileSavedViewModel(
    user: UserModel,
): ProfileSavedViewModel =
    ProfileScreenModelHelper.getSavedModel(user = user)

actual fun getProfileCommentsViewModel(user: UserModel): ProfileCommentsViewModel =
    ProfileScreenModelHelper.getCommentsModel(user)

object ProfileScreenModelHelper : KoinComponent {
    val profileModel: ProfileContentViewModel by inject()
    val loginModel: LoginBottomSheetViewModel by inject()
    val loggedModel: ProfileLoggedViewModel by inject()

    fun getPostsModel(user: UserModel): ProfilePostsViewModel {
        val res: ProfilePostsViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return res
    }

    fun getCommentsModel(user: UserModel): ProfileCommentsViewModel {
        val res: ProfileCommentsViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return res
    }

    fun getSavedModel(user: UserModel): ProfileSavedViewModel {
        val res: ProfileSavedViewModel by inject(
            parameters = { parametersOf(user) },
        )
        return res
    }
}
