package com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime

expect fun epochMillis(): Long

expect fun getFormattedDate(
    iso8601Timestamp: String,
    format: String,
): String

expect fun getPrettyDate(
    iso8601Timestamp: String,
    yearLabel: String,
    monthLabel: String,
    dayLabel: String,
    hourLabel: String,
    minuteLabel: String,
    secondLabel: String,
): String
