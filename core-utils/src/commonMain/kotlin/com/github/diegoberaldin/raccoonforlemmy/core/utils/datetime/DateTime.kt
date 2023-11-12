package com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime

expect object DateTime {
    fun epochMillis(): Long

    fun getFormattedDate(
        iso8601Timestamp: String,
        format: String,
    ): String

    fun getPrettyDate(
        iso8601Timestamp: String,
        yearLabel: String,
        monthLabel: String,
        dayLabel: String,
        hourLabel: String,
        minuteLabel: String,
        secondLabel: String,
    ): String
}
