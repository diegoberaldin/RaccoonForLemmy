package com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.autoupdatingCurrentLocale
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970

actual object DateTime {

    actual fun epochMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun getFormattedDate(
        iso8601Timestamp: String,
        format: String,
    ): String {
        val date = getDateFromIso8601Timestamp(iso8601Timestamp) ?: return ""

        val dateFormatter = NSDateFormatter()
        dateFormatter.timeZone = NSTimeZone.localTimeZone
        dateFormatter.locale = NSLocale.autoupdatingCurrentLocale
        dateFormatter.dateFormat = format
        return dateFormatter.stringFromDate(date)
    }

    actual fun getPrettyDate(
        iso8601Timestamp: String,
        yearLabel: String,
        monthLabel: String,
        dayLabel: String,
        hourLabel: String,
        minuteLabel: String,
        secondLabel: String,
    ): String {
        val date = getDateFromIso8601Timestamp(iso8601Timestamp) ?: return ""
        val now = NSDate()
        val calendar = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
        val delta = calendar.components(
            unitFlags = NSCalendarUnitSecond.or(NSCalendarUnitMinute).or(NSCalendarUnitHour)
                .or(NSCalendarUnitDay).or(NSCalendarUnitMonth).or(NSCalendarUnitYear),
            fromDate = date,
            toDate = now,
            options = 0u,
        )
        return when {
            delta.year >= 1 -> buildString {
                append("${delta.year}$yearLabel")
                if (delta.month >= 1) {
                    append(" ${delta.month}$monthLabel")
                }
                if (delta.day >= 1) {
                    append(" ${delta.day}$dayLabel")
                }
            }

            delta.month >= 1 -> buildString {
                append("${delta.month}$monthLabel")
                if (delta.day >= 1) {
                    append(" ${delta.day}$dayLabel")
                }
            }

            delta.day >= 1 -> buildString {
                append("${delta.day}$dayLabel")
            }

            delta.hour >= 1 -> buildString {
                append(" ${delta.hour}$hourLabel")
            }

            delta.minute >= 1 -> buildString {
                append(" ${delta.minute}$minuteLabel")
            }

            else -> buildString {
                append(" ${delta.second}$secondLabel")
            }
        }
    }

    private fun getDateFromIso8601Timestamp(string: String): NSDate? {
        return NSISO8601DateFormatter().dateFromString(string)
    }
}