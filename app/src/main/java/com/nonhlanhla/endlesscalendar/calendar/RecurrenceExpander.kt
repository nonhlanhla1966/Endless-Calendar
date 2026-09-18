package com.nonhlanhla.endlesscalendar.calendar

import com.nonhlanhla.endlesscalendar.data.Event
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * A single occurrence of an [Event] on the calendar — either the event itself
 * (non-recurring) or one instance generated from its recurrence rule.
 */
data class Occurrence(
    val event: Event,
    val startMillis: Long,
    val endMillis: Long
)

/**
 * Expands a simplified RRULE (FREQ, INTERVAL, COUNT, UNTIL, BYDAY) into concrete
 * occurrences that fall within a date range. This intentionally supports the
 * common subset of RFC 5545 rather than the full specification.
 */
object RecurrenceExpander {

    private data class Rule(
        val freq: String,
        val interval: Int,
        val count: Int?,
        val until: Long?,
        val byDay: Set<DayOfWeek>
    )

    private fun parseRule(rrule: String): Rule? {
        if (rrule.isBlank()) return null
        val parts = rrule.split(";").mapNotNull {
            val kv = it.split("=", limit = 2)
            if (kv.size == 2) kv[0].trim().uppercase() to kv[1].trim() else null
        }.toMap()

        val freq = parts["FREQ"] ?: return null
        val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
        val count = parts["COUNT"]?.toIntOrNull()
        val until = parts["UNTIL"]?.toLongOrNull()
        val byDay = parts["BYDAY"]?.split(",")?.mapNotNull { code ->
            mapOf(
                "MO" to DayOfWeek.MONDAY, "TU" to DayOfWeek.TUESDAY, "WE" to DayOfWeek.WEDNESDAY,
                "TH" to DayOfWeek.THURSDAY, "FR" to DayOfWeek.FRIDAY, "SA" to DayOfWeek.SATURDAY,
                "SU" to DayOfWeek.SUNDAY
            )[code]
        }?.toSet() ?: emptySet()

        return Rule(freq, interval.coerceAtLeast(1), count, until, byDay)
    }

    /** Returns occurrences of [event] that intersect [rangeStart, rangeEnd] (millis, inclusive). */
    fun expand(event: Event, rangeStart: Long, rangeEnd: Long, maxOccurrences: Int = 2000): List<Occurrence> {
        val duration = (event.endMillis - event.startMillis).coerceAtLeast(0)
        val rule = parseRule(event.recurrenceRule)
            ?: return if (event.startMillis <= rangeEnd && event.endMillis >= rangeStart) {
                listOf(Occurrence(event, event.startMillis, event.endMillis))
            } else emptyList()

        val zone = runCatching { ZoneId.of(event.timeZoneId) }.getOrDefault(ZoneId.systemDefault())
        var cursor = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.startMillis), zone)
        val untilLimit = rule.until ?: Long.MAX_VALUE
        val results = mutableListOf<Occurrence>()
        var produced = 0
        var iterations = 0
        val maxIterations = maxOccurrences * 8 + 4000

        while (iterations < maxIterations) {
            iterations++
            val occStart = cursor.toInstant().toEpochMilli()
            if (occStart > rangeEnd || occStart > untilLimit) break
            if (rule.count != null && produced >= rule.count) break

            val matchesByDay = rule.byDay.isEmpty() || rule.byDay.contains(cursor.dayOfWeek)
            if (matchesByDay) {
                produced++
                val occEnd = occStart + duration
                if (occEnd >= rangeStart && occStart <= rangeEnd) {
                    results.add(Occurrence(event, occStart, occEnd))
                }
                if (results.size >= maxOccurrences) break
            }

            cursor = when (rule.freq) {
                "DAILY" -> cursor.plusDays(rule.interval.toLong())
                "WEEKLY" -> {
                    if (rule.byDay.isNotEmpty()) cursor.plusDays(1)
                    else cursor.plusWeeks(rule.interval.toLong())
                }
                "MONTHLY" -> cursor.plusMonths(rule.interval.toLong())
                "YEARLY" -> cursor.plusYears(rule.interval.toLong())
                else -> return results
            }
        }
        return results
    }

    fun expandAll(events: List<Event>, rangeStart: Long, rangeEnd: Long): List<Occurrence> =
        events.flatMap { expand(it, rangeStart, rangeEnd) }.sortedBy { it.startMillis }
}
