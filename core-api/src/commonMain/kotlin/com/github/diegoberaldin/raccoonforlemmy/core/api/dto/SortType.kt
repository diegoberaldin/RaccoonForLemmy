package com.github.diegoberaldin.raccoonforlemmy.core.api.dto

import kotlinx.serialization.SerialName

enum class SortType {
    @SerialName("Active")
    Active,

    @SerialName("Hot")
    Hot,

    @SerialName("New")
    New,

    @SerialName("Old")
    Old,

    @SerialName("TopDay")
    TopDay,

    @SerialName("TopWeek")
    TopWeek,

    @SerialName("TopMonth")
    TopMonth,

    @SerialName("TopYear")
    TopYear,

    @SerialName("TopAll")
    TopAll,

    @SerialName("Top")
    Top,

    @SerialName("MostComments")
    MostComments,

    @SerialName("NewComments")
    NewComments,

    @SerialName("TopHour")
    TopHour,

    @SerialName("TopSixHour")
    TopSixHour,

    @SerialName("TopTwelveHour")
    TopTwelveHour,

    @SerialName("TopThreeMonths")
    TopThreeMonths,

    @SerialName("TopSixMonths")
    TopSixMonths,

    @SerialName("TopNineMonths")
    TopNineMonths,

    @SerialName("Controversial")
    Controversial,

    @SerialName("Scaled")
    Scaled,
}
