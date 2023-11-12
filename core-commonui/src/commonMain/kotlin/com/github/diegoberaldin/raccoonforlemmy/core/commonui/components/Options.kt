package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

data class Option(
    val id: OptionId,
    val text: String,
)

sealed class OptionId(val value: Int) {
    data object Share : OptionId(0)
    data object Hide : OptionId(1)
    data object SeeRaw : OptionId(2)
    data object CrossPost : OptionId(3)
    data object Report : OptionId(4)
    data object Edit : OptionId(5)
    data object Delete : OptionId(6)
    data object Info : OptionId(7)
    data object InfoInstance : OptionId(8)
    data object Block : OptionId(9)
    data object BlockInstance : OptionId(10)
    data object MarkRead : OptionId(11)
    data object MarkUnread : OptionId(12)
}