package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail

sealed interface UserDetailSection {
    object Posts : UserDetailSection
    object Comments : UserDetailSection
}
