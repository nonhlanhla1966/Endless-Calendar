package com.nonhlanhla.endlesscalendar.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * The calendar "scrolls forever" by mapping a huge but bounded list index to a
 * date relative to a fixed anchor. At ~2000 years in each direction this is far
 * beyond any practical use, and Compose's LazyColumn only ever composes the
 * handful of items actually on screen, so there is no performance cost to the
 * size of the range.
 */
object CalendarMath {
    private val ANCHOR_MONTH: YearMonth = YearMonth.of(2000, 1)
    private val ANCHOR_DAY: LocalDate = LocalDate.of(2000, 1, 1)

    const val MONTH_RADIUS = 24_000 // ~2000 years each direction
    const val MONTH_CENTER = MONTH_RADIUS
    const val MONTH_ITEM_COUNT = MONTH_RADIUS * 2 + 1

    const val DAY_RADIUS = 730_000 // ~2000 years each direction
    const val DAY_CENTER = DAY_RADIUS
    const val DAY_ITEM_COUNT = DAY_RADIUS * 2 + 1

    const val WEEK_RADIUS = 104_000 // ~2000 years each direction
    const val WEEK_CENTER = WEEK_RADIUS
    const val WEEK_ITEM_COUNT = WEEK_RADIUS * 2 + 1

    fun monthForIndex(index: Int): YearMonth = ANCHOR_MONTH.plusMonths((index - MONTH_CENTER).toLong())
    fun indexForMonth(month: YearMonth): Int =
        (MONTH_CENTER + java.time.temporal.ChronoUnit.MONTHS.between(ANCHOR_MONTH, month)).toInt()

    fun dayForIndex(index: Int): LocalDate = ANCHOR_DAY.plusDays((index - DAY_CENTER).toLong())
    fun indexForDay(date: LocalDate): Int =
        (DAY_CENTER + java.time.temporal.ChronoUnit.DAYS.between(ANCHOR_DAY, date)).toInt()

    fun weekStartForIndex(index: Int, weekStartsMonday: Boolean): LocalDate {
        val base = ANCHOR_DAY.with(if (weekStartsMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY)
        return base.plusWeeks((index - WEEK_CENTER).toLong())
    }

    fun indexForWeek(date: LocalDate, weekStartsMonday: Boolean): Int {
        val weekStart = date.with(if (weekStartsMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY)
        val base = ANCHOR_DAY.with(if (weekStartsMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY)
        return (WEEK_CENTER + java.time.temporal.ChronoUnit.WEEKS.between(base, weekStart)).toInt()
    }

    /** All calendar cells for a month grid, including the leading/trailing days of adjacent months. */
    fun gridDaysForMonth(month: YearMonth, weekStartsMonday: Boolean): List<LocalDate> {
        val firstOfMonth = month.atDay(1)
        val startDow = firstOfMonth.dayOfWeek
        val leading = if (weekStartsMonday) {
            (startDow.value - DayOfWeek.MONDAY.value + 7) % 7
        } else {
            (startDow.value - DayOfWeek.SUNDAY.value + 7) % 7
        }
        val gridStart = firstOfMonth.minusDays(leading.toLong())
        val totalCells = 42 // 6 full weeks, consistent row count
        return (0 until totalCells).map { gridStart.plusDays(it.toLong()) }
    }

    fun startOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDayMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
}
