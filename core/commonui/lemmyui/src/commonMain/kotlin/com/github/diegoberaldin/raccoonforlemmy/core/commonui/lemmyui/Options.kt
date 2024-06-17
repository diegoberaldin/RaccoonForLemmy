package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

data class Option(
    val id: OptionId,
    val text: String,
)

sealed class OptionId(
    val value: Int,
) {
    data object Share : OptionId(0)

    data object Hide : OptionId(1)

    data object SeeRaw : OptionId(2)

    data object CrossPost : OptionId(3)

    data object Report : OptionId(4)

    data object Edit : OptionId(5)

    data object Delete : OptionId(6)

    data object Purge : OptionId(7)

    data object InfoInstance : OptionId(8)

    data object Block : OptionId(9)

    data object BlockInstance : OptionId(10)

    data object AdminFeaturePost : OptionId(11)

    data object SetPreferredLanguage : OptionId(12)

    data object FeaturePost : OptionId(13)

    data object LockPost : OptionId(14)

    data object Remove : OptionId(15)

    data object DistinguishComment : OptionId(16)

    data object OpenReports : OptionId(17)

    data object ResolveReport : OptionId(18)

    data object BanUser : OptionId(19)

    data object AddMod : OptionId(20)

    data object Favorite : OptionId(21)

    data object ViewModlog : OptionId(22)

    data object Unban : OptionId(23)

    data object SetCustomSort : OptionId(24)

    data object Search : OptionId(25)

    data object Copy : OptionId(26)

    data object ExploreInstance : OptionId(27)

    data object Unsubscribe : OptionId(28)

    data object PurgeCreator : OptionId(29)

    data object Restore : OptionId(30)
}
