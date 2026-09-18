package com.nonhlanhla.endlesscalendar.data

import java.time.LocalDate

enum class HolidayRegion { US, UK, SOUTH_AFRICA, NONE }

data class Holiday(val date: LocalDate, val name: String)

/**
 * Computes public holidays for a given year without any network access:
 * fixed-date holidays directly, and Easter-derived ones via the anonymous
 * Gregorian algorithm (Meeus/Jones/Butcher).
 */
object HolidayProvider {

    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    fun holidaysFor(region: HolidayRegion, year: Int): List<Holiday> {
        if (region == HolidayRegion.NONE) return emptyList()
        val easter = easterSunday(year)
        val goodFriday = easter.minusDays(2)
        val list = mutableListOf<Holiday>()

        when (region) {
            HolidayRegion.US -> {
                list += Holiday(LocalDate.of(year, 1, 1), "New Year's Day")
                list += Holiday(LocalDate.of(year, 7, 4), "Independence Day")
                list += Holiday(LocalDate.of(year, 11, 11), "Veterans Day")
                list += Holiday(LocalDate.of(year, 12, 25), "Christmas Day")
                list += Holiday(nthWeekday(year, 11, java.time.DayOfWeek.THURSDAY, 4), "Thanksgiving")
                list += Holiday(nthWeekday(year, 9, java.time.DayOfWeek.MONDAY, 1), "Labor Day")
                list += Holiday(nthWeekday(year, 5, java.time.DayOfWeek.MONDAY, -1), "Memorial Day")
            }
            HolidayRegion.UK -> {
                list += Holiday(LocalDate.of(year, 1, 1), "New Year's Day")
                list += Holiday(goodFriday, "Good Friday")
                list += Holiday(easter.plusDays(1), "Easter Monday")
                list += Holiday(nthWeekday(year, 5, java.time.DayOfWeek.MONDAY, 1), "Early May Bank Holiday")
                list += Holiday(LocalDate.of(year, 12, 25), "Christmas Day")
                list += Holiday(LocalDate.of(year, 12, 26), "Boxing Day")
            }
            HolidayRegion.SOUTH_AFRICA -> {
                list += Holiday(LocalDate.of(year, 1, 1), "New Year's Day")
                list += Holiday(LocalDate.of(year, 3, 21), "Human Rights Day")
                list += Holiday(goodFriday, "Good Friday")
                list += Holiday(LocalDate.of(year, 4, 27), "Freedom Day")
                list += Holiday(LocalDate.of(year, 5, 1), "Workers' Day")
                list += Holiday(LocalDate.of(year, 6, 16), "Youth Day")
                list += Holiday(LocalDate.of(year, 8, 9), "National Women's Day")
                list += Holiday(LocalDate.of(year, 9, 24), "Heritage Day")
                list += Holiday(LocalDate.of(year, 12, 16), "Day of Reconciliation")
                list += Holiday(LocalDate.of(year, 12, 25), "Christmas Day")
                list += Holiday(LocalDate.of(year, 12, 26), "Day of Goodwill")
            }
            HolidayRegion.NONE -> {}
        }
        return list
    }

    private fun nthWeekday(year: Int, month: Int, day: java.time.DayOfWeek, occurrence: Int): LocalDate {
        val first = LocalDate.of(year, month, 1)
        if (occurrence > 0) {
            var date = first
            var count = 0
            while (true) {
                if (date.dayOfWeek == day) {
                    count++
                    if (count == occurrence) return date
                }
                date = date.plusDays(1)
            }
        } else {
            var date = first.withDayOfMonth(first.lengthOfMonth())
            while (date.dayOfWeek != day) date = date.minusDays(1)
            return date
        }
    }
}
