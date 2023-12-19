package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail

sealed interface UserDetailSection {
    data object Posts : UserDetailSection
    data object Comments : UserDetailSection
}
